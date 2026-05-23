package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.MyApplicationTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ImportantDevices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.data.ReceiptEntity
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.collectAsState

import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.ReceiptRepository

class MainActivity : ComponentActivity() {
    private var db: AppDatabase? = null
    private var repository: ReceiptRepository? = null
    private var databaseInitError: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup custom uncaught exception handler to log crashes
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = java.io.File(cacheDir, "crash_log.txt")
                java.io.StringWriter().use { sw ->
                    java.io.PrintWriter(sw).use { pw ->
                        throwable.printStackTrace(pw)
                    }
                    file.writeText(sw.toString())
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            if (oldHandler != null) {
                oldHandler.uncaughtException(thread, throwable)
            } else {
                System.exit(1)
            }
        }

        var dbTemp: AppDatabase? = null
        var repoTemp: ReceiptRepository? = null
        try {
            dbTemp = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "receipt_db")
                .fallbackToDestructiveMigration()
                .build()
            repoTemp = ReceiptRepository(dbTemp.receiptDao(), dbTemp.templateDao())
        } catch (e: Throwable) {
            e.printStackTrace()
            databaseInitError = "First attempt error:\n" + e.stackTraceToString()
            try {
                applicationContext.deleteDatabase("receipt_db")
                dbTemp = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "receipt_db")
                    .fallbackToDestructiveMigration()
                    .build()
                repoTemp = ReceiptRepository(dbTemp.receiptDao(), dbTemp.templateDao())
            } catch (e2: Throwable) {
                e2.printStackTrace()
                databaseInitError += "\nSecond attempt error:\n" + e2.stackTraceToString()
            }
        }
        db = dbTemp
        repository = repoTemp
        
        enableEdgeToEdge()
        val printerService = PrinterService(this)
        setContent {
            val systemTheme = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemTheme) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                val currentRepo = repository
                if (currentRepo == null) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Text("Database Initialization Error", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Text("We suffered a critical failure starting the local receipt database. Please try restarting the application.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                            
                            databaseInitError?.let { errTrace ->
                                Spacer(Modifier.height(16.dp))
                                Text("Diagnostic Details:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                        .verticalScroll(rememberScrollState())
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = errTrace,
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        ReceiptApp(
                            printerService = printerService,
                            repository = currentRepo,
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrintPreviewBox(
    headerTitle: String, headerWebsite: String, headerPhone: String, headerEmail: String,
    serviceDetails: String, amount: String, customText: String, footerText: String, paperSize: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val previewWidth = if (paperSize == "80mm") 240.dp else 180.dp
            CompositionLocalProvider(LocalContentColor provides Color(0xFF1D1B20)) {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .border(1.dp, Color.LightGray)
                        .padding(16.dp)
                        .width(previewWidth)
                        .animateContentSize()
                ) {
                    Column {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            if(headerTitle.isNotBlank()) Text(headerTitle, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                            if(headerWebsite.isNotBlank()) Text(headerWebsite, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp))
                            if(headerPhone.isNotBlank()) Text(headerPhone, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp))
                            if(headerEmail.isNotBlank()) Text(headerEmail, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Service:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                            Text(if (serviceDetails.isBlank()) "Doc Fee" else serviceDetails, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                            Text("Rs. " + (if (amount.isBlank()) "125.00" else amount), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                        }
                        if (customText.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Notes:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                            Text(customText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                        Spacer(Modifier.height(16.dp))
                        if(footerText.isNotBlank()) Text(footerText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Print Preview", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptApp(printerService: PrinterService, repository: ReceiptRepository, isDarkMode: Boolean, onThemeToggle: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val checkBluetoothPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    var bluetoothPermissionGranted by remember {
        mutableStateOf(checkBluetoothPermission())
    }
    
    var showDeviceDialog by remember { mutableStateOf(false) }
    var pairedDevicesList by remember { mutableStateOf<List<PrinterDevice>>(emptyList()) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val granted = permissionsMap[Manifest.permission.BLUETOOTH_CONNECT] ?: false
            bluetoothPermissionGranted = granted
            if (granted) {
                pairedDevicesList = printerService.getPairedDevices()
                showDeviceDialog = true
            } else {
                Toast.makeText(context, "Bluetooth permission is required to search and connect to printers", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    var customerName by remember { mutableStateOf("") }
    var serviceDetails by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var customText by remember { mutableStateOf("") }
    var crashLog by remember { mutableStateOf<String?>(null) }
    
    val templates by repository.allTemplates.collectAsState(initial = emptyList())
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val file = java.io.File(context.cacheDir, "crash_log.txt")
                if (file.exists()) {
                    val log = file.readText()
                    withContext(Dispatchers.Main) {
                        crashLog = log
                    }
                }
                
                if (repository.getTemplateCount() == 0) {
                    repository.insertTemplate(com.example.data.TemplateEntity(
                        name = "Naeem Documentation",
                        headerTitle = "NAEEM DOCUMENTATION",
                        headerWebsite = "naeemdocumentation.com",
                        headerPhone = "Phone: 0315 8157721",
                        headerEmail = "Email: naeemdocumentation@gmail.com",
                        footerText = "THANK YOU FOR YOUR BUSINESS",
                        paperSize = "58mm",
                        isSelected = true
                    ))
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                try {
                    val file = java.io.File(context.cacheDir, "crash_log.txt")
                    java.io.StringWriter().use { sw ->
                        java.io.PrintWriter(sw).use { pw ->
                            e.printStackTrace(pw)
                        }
                        file.writeText(sw.toString())
                    }
                    val log = file.readText()
                    withContext(Dispatchers.Main) {
                        crashLog = log
                    }
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }
        }
    }
    
    val activeTemplate = templates.find { it.isSelected } ?: templates.firstOrNull()
    
    // For Customize Tab Editor
    var editTemplateId by remember { mutableIntStateOf(0) }
    var editTemplateName by remember { mutableStateOf("New Template") }
    var headerTitle by remember { mutableStateOf("") }
    var headerWebsite by remember { mutableStateOf("") }
    var headerPhone by remember { mutableStateOf("") }
    var headerEmail by remember { mutableStateOf("") }
    var footerText by remember { mutableStateOf("") }
    val paperSizes = listOf("58mm", "80mm")
    var selectedPaperSize by remember { mutableStateOf(paperSizes[0]) }

    fun loadIntoEditor(template: com.example.data.TemplateEntity?) {
        if (template != null) {
            editTemplateId = template.id
            editTemplateName = template.name
            headerTitle = template.headerTitle
            headerWebsite = template.headerWebsite
            headerPhone = template.headerPhone
            headerEmail = template.headerEmail
            footerText = template.footerText
            selectedPaperSize = template.paperSize
        } else {
            editTemplateId = 0
            editTemplateName = "New Template"
            headerTitle = ""
            headerWebsite = ""
            headerPhone = ""
            headerEmail = ""
            footerText = ""
            selectedPaperSize = paperSizes[0]
        }
    }

    LaunchedEffect(activeTemplate) {
        if (activeTemplate != null && editTemplateId == 0) {
            loadIntoEditor(activeTemplate)
        }
    }
    
    val receipts by repository.allReceipts.collectAsState(initial = emptyList())
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    var isConnected by remember { mutableStateOf(false) }
    var connectedDeviceName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier.fillMaxSize().animateContentSize(animationSpec = tween(300))
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = "Naeem Documentation Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Naeem Docs",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isConnected) Color(0xFFD1E8CF) else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            try {
                                val isGranted = checkBluetoothPermission()
                                bluetoothPermissionGranted = isGranted
                                if (isGranted) {
                                    if (isConnected) {
                                        printerService.disconnect()
                                        isConnected = false
                                        connectedDeviceName = null
                                    } else {
                                        pairedDevicesList = printerService.getPairedDevices()
                                        showDeviceDialog = true
                                    }
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        launcher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = if (isConnected) Color(0xFF0A2F07) else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isConnected) (connectedDeviceName ?: "Connected") else "Connect",
                            color = if (isConnected) Color(0xFF0A2F07) else MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable { onThemeToggle() }
                        .padding(4.dp)
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Main Content Area
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            if (selectedTab == 0) {
                // Receipt Configuration Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color.LightGray)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "RECEIPT CONFIGURATION",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Amount (Rs.)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = serviceDetails,
                            onValueChange = { serviceDetails = it },
                            label = { Text("Service Item (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Client Name (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = customText,
                            onValueChange = { customText = it },
                            label = { Text("Custom Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                }
                
                activeTemplate?.let { tpl ->
                    PrintPreviewBox(
                        headerTitle = tpl.headerTitle,
                        headerWebsite = tpl.headerWebsite,
                        headerPhone = tpl.headerPhone,
                        headerEmail = tpl.headerEmail,
                        serviceDetails = serviceDetails,
                        amount = amount,
                        customText = customText,
                        footerText = tpl.footerText,
                        paperSize = tpl.paperSize
                    )
                }
            } else if (selectedTab == 1) {
                // Templates List
                Text("Saved Templates", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = editTemplateId == 0,
                            onClick = { loadIntoEditor(null) },
                            label = { Text("New Template") }
                        )
                    }
                    items(templates) { tpl ->
                        FilterChip(
                            selected = editTemplateId == tpl.id,
                            onClick = { loadIntoEditor(tpl) },
                            label = { Text(tpl.name) },
                            leadingIcon = if (tpl.isSelected) { { Icon(Icons.Default.Check, null) } } else null
                        )
                    }
                }
                
                // Formatting Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color.LightGray)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                         Text(
                            text = "CUSTOMIZE BRANDING",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = editTemplateName,
                            onValueChange = { editTemplateName = it },
                            label = { Text("Template Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = headerTitle,
                            onValueChange = { headerTitle = it },
                            label = { Text("Store Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = headerPhone,
                            onValueChange = { headerPhone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = headerEmail,
                            onValueChange = { headerEmail = it },
                            label = { Text("Email (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = headerWebsite,
                            onValueChange = { headerWebsite = it },
                            label = { Text("Website (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = footerText,
                            onValueChange = { footerText = it },
                            label = { Text("Footer Message") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Paper Size:", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(8.dp))
                            paperSizes.forEach { size ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedPaperSize = size }
                                        .padding(end = 12.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    RadioButton(selected = selectedPaperSize == size, onClick = { selectedPaperSize = size })
                                    Text(size, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (editTemplateName.isBlank()) {
                                        Toast.makeText(context, "Template Name required", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    coroutineScope.launch {
                                        repository.insertTemplate(
                                            com.example.data.TemplateEntity(
                                                id = editTemplateId,
                                                name = editTemplateName,
                                                headerTitle = headerTitle,
                                                headerWebsite = headerWebsite,
                                                headerPhone = headerPhone,
                                                headerEmail = headerEmail,
                                                footerText = footerText,
                                                paperSize = selectedPaperSize,
                                                isSelected = activeTemplate?.id == editTemplateId || templates.isEmpty()
                                            )
                                        )
                                        Toast.makeText(context, "Template Saved", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Save") }
                            if (editTemplateId != 0 && activeTemplate?.id != editTemplateId) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.selectTemplate(editTemplateId)
                                            Toast.makeText(context, "Set as Active", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Set Active") }
                            }
                        }
                    }
                }
                
                PrintPreviewBox(
                    headerTitle = headerTitle,
                    headerWebsite = headerWebsite,
                    headerPhone = headerPhone,
                    headerEmail = headerEmail,
                    serviceDetails = serviceDetails,
                    amount = amount,
                    customText = customText,
                    footerText = footerText,
                    paperSize = selectedPaperSize
                )
            } else if (selectedTab == 2) {
                // History Card
                val totalSales = receipts.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color.LightGray)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                         Text(
                            text = "ANALYTICS",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Receipts:", style = MaterialTheme.typography.bodyMedium)
                            Text("${receipts.size}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Sales:", style = MaterialTheme.typography.bodyMedium)
                            Text("Rs. ${String.format(Locale.US, "%.2f", totalSales)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
                
                Text(
                    text = "RECENT RECEIPTS",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, start = 4.dp)
                )
                
                val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
                
                if (receipts.isEmpty()) {
                    Text("No receipts printed yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(start = 4.dp))
                } else {
                    receipts.take(50).forEach { receipt ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(receipt.serviceDetails, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                    Text("Rs. ${receipt.amount}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                if (receipt.customerName.isNotBlank()) {
                                    Text("Customer: ${receipt.customerName}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Text("Date: ${dateFormat.format(Date(receipt.timestamp))}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
        
        // Footer Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTab == 0) {
                Button(
                    onClick = {
                        if (!isConnected) {
                            Toast.makeText(context, "Please connect to a printer first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (amount.isBlank()) {
                            Toast.makeText(context, "Amount is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val finalService = if (serviceDetails.isBlank()) "Doc Fee" else serviceDetails
                        
                        coroutineScope.launch {
                            val success = withContext(Dispatchers.IO) {
                                val tpl = activeTemplate
                                val pHeaderTitle = tpl?.headerTitle ?: "NAEEM DOCUMENTATION"
                                val pHeaderWebsite = tpl?.headerWebsite ?: ""
                                val pHeaderPhone = tpl?.headerPhone ?: ""
                                val pHeaderEmail = tpl?.headerEmail ?: ""
                                val pFooterText = tpl?.footerText ?: "THANK YOU"
                                val pPaperSize = tpl?.paperSize ?: "58mm"

                                val printed = printerService.printReceipt(
                                    customerName, finalService, amount, customText,
                                    pHeaderTitle, pHeaderWebsite, pHeaderPhone, pHeaderEmail, pFooterText, pPaperSize
                                )
                                if (printed) {
                                    repository.insert(ReceiptEntity(
                                        customerName = customerName,
                                        serviceDetails = finalService,
                                        amount = amount
                                    ))
                                }
                                printed
                            }
                            if (success) {
                                Toast.makeText(context, "Receipt Printed!", Toast.LENGTH_SHORT).show()
                                customerName = ""
                                serviceDetails = ""
                                amount = ""
                            } else {
                                Toast.makeText(context, "Failed to print", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(50)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = isConnected && amount.isNotBlank()
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Print", tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("PRINT RECEIPT", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
            
            // Bottom Nav
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedTab = 0 }) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = if(selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                    Text("New Print", style = MaterialTheme.typography.labelSmall, color = if(selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedTab = 1 }) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = if(selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                    Text("Customize", style = MaterialTheme.typography.labelSmall, color = if(selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedTab = 2 }) {
                    Icon(Icons.Default.History, contentDescription = null, tint = if(selectedTab == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                    Text("History", style = MaterialTheme.typography.labelSmall, color = if(selectedTab == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                    try {
                        val isGranted = checkBluetoothPermission()
                        bluetoothPermissionGranted = isGranted
                        if (isGranted) {
                            pairedDevicesList = printerService.getPairedDevices()
                            showDeviceDialog = true 
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                launcher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
                            }
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }) {
                    Icon(Icons.Default.ImportantDevices, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Text("Devices", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            
            // Footer
            Text(
                text = buildAnnotatedString {
                    append("Made with ❤️ by ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("Abdul Hayy Khan")
                    }
                },
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable {
                        try {
                            uriHandler.openUri("https://www.linkedin.com/in/abdulhayykhan/")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
                        }
                    },
                textAlign = TextAlign.Center
            )
        }
    }
    
    // ... rest of dialog logic
    if (showDeviceDialog) {
        DeviceSelectionDialog(
            devices = pairedDevicesList,
            onDismiss = { showDeviceDialog = false },
            onDeviceSelected = { device ->
                showDeviceDialog = false
                isLoading = true
                coroutineScope.launch(Dispatchers.IO) {
                    val success = printerService.connect(device)
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        if (success) {
                            isConnected = true
                            connectedDeviceName = device.name
                            Toast.makeText(context, "Connected to ${connectedDeviceName}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Connection Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }
    
    if (isLoading) {
        Dialog(onDismissRequest = { }) {
            CircularProgressIndicator()
        }
    }

    if (crashLog != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("App Diagnostics ⚙️") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("The application encountered an unexpected error on a previous run. Below is the debug trace:", style = MaterialTheme.typography.bodyMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Text(
                            text = crashLog ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val file = java.io.File(context.cacheDir, "crash_log.txt")
                            if (file.exists()) {
                                file.delete()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        crashLog = null
                    }
                ) {
                    Text("Clear & Continue")
                }
            }
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSelectionDialog(
    devices: List<PrinterDevice>,
    onDismiss: () -> Unit,
    onDeviceSelected: (PrinterDevice) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Printer") },
        text = {
            if (devices.isEmpty()) {
                Text("No paired Bluetooth devices found. Please pair a printer in Android settings first.")
            } else {
                LazyColumn {
                    items(devices) { device ->
                        ListItem(
                            headlineContent = { Text(device.name) },
                            supportingContent = { Text(device.address) },
                            modifier = Modifier.clickable { onDeviceSelected(device) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

