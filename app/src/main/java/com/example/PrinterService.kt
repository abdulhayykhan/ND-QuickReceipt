package com.example

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PrinterDevice(val name: String, val address: String, val device: BluetoothDevice)

class PrinterService(private val context: Context) {
    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager?.adapter
    }
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    // Standard SPP UUID
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<PrinterDevice> {
        return try {
            bluetoothAdapter?.bondedDevices?.map { 
                val name = try { it.name ?: it.address } catch (e: Exception) { it.address }
                PrinterDevice(name, it.address, it)
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(printerDevice: PrinterDevice): Boolean {
        return try {
            bluetoothSocket?.close()
            bluetoothSocket = printerDevice.device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: Exception) {
            e.printStackTrace()
            outputStream = null
            bluetoothSocket = null
            false
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        outputStream = null
        bluetoothSocket = null
    }

    fun isConnected() = bluetoothSocket?.isConnected == true

    fun printReceipt(customerName: String, service: String, amount: String, customText: String, headerTitle: String, headerWebsite: String, headerPhone: String, headerEmail: String, footerText: String, paperSize: String): Boolean {
        return try {
            if (outputStream == null) return false
            
            val lineLength = if (paperSize == "80mm") 48 else 32
            val separator = "-".repeat(lineLength) + "\n"
            
            // Init printer
            outputStream?.write(byteArrayOf(0x1B, 0x40)) 
            
            // Center align for title
            outputStream?.write(byteArrayOf(0x1B, 0x61, 0x01))
            outputStream?.write(separator.toByteArray())
            if (headerTitle.isNotBlank()) outputStream?.write("$headerTitle\n".toByteArray())
            if (headerWebsite.isNotBlank()) outputStream?.write("$headerWebsite\n".toByteArray())
            if (headerPhone.isNotBlank()) outputStream?.write("$headerPhone\n".toByteArray())
            if (headerEmail.isNotBlank()) outputStream?.write("$headerEmail\n".toByteArray())
            outputStream?.write(separator.toByteArray())
            
            // Left align for body
            outputStream?.write(byteArrayOf(0x1B, 0x61, 0x00))
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            outputStream?.write("Date: $date\n\n".toByteArray())
            
            if (customerName.isNotBlank()) outputStream?.write("Customer: $customerName\n".toByteArray())
            outputStream?.write("Service: $service\n".toByteArray())
            outputStream?.write("Amount: Rs. $amount\n\n".toByteArray())
            
            if (customText.isNotBlank()) {
                outputStream?.write("Notes: $customText\n\n".toByteArray())
            }
            
            // Center align for footer
            outputStream?.write(byteArrayOf(0x1B, 0x61, 0x01))
            if (footerText.isNotBlank()) outputStream?.write("$footerText\n".toByteArray())
            outputStream?.write(separator.toByteArray())
            outputStream?.write("\n\n\n".toByteArray())
            
            // Cut
            outputStream?.write(byteArrayOf(0x1D, 0x56, 0x41, 0x10))
            outputStream?.flush()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
