package com.example.filemanager.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.filemanager.MainViewModel
import com.example.filemanager.R
import com.example.filemanager.data.FileManagerDataStoreInterface
import com.example.filemanager.ui.theme.FileManagerTheme
import com.example.filemanager.utils.Converters.findAppropriateDrawable
import com.example.filemanager.utils.Routes
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var fileManagerDataStore: FileManagerDataStoreInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FileManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val mainViewModel = hiltViewModel<MainViewModel>()
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Routes.Welcome.route) {
                        composable(Routes.MainScreen.route) {
                            MainScreen(
                                mainViewModel,
                                openFile = {
                                    openFile(it)
                                },
                                shareFile = {
                                    shareFile(it)
                                }
                            )
                        }
                        composable(Routes.Welcome.route) {
                            WelcomeScreen(navController = navController)
                            PermissionGrant(fileManagerDataStore, mainViewModel, grantPermission = {
                                grantPermission()
                            })
                        }
                    }
                }
            }
        }
    }

    private fun grantPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:" + applicationContext.packageName)
                }
                startActivity(intent)
            } else {
                // Permission is already granted
            }
        }
    }

    private fun openFile(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, getMimeType(file.extension))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            applicationContext,
            "${applicationContext.packageName}.provider",
            file
        )

        val mimeType = getMimeType(file.extension)
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            setType(mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Share file"))
    }

    private fun getMimeType(extension: String): String? {
        val mimeTypeMap = android.webkit.MimeTypeMap.getSingleton()
        return mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT))
    }

}

@Composable
fun PermissionGrant(fileManagerDataStore: FileManagerDataStoreInterface, mainViewModel: MainViewModel, grantPermission: () -> Unit) {
    var showWarnDialog by remember { mutableStateOf(false) }
    val firstEntry = fileManagerDataStore.readFirstEntranceVal.collectAsState(initial = false)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && firstEntry.value) {
        AskForPermissionDialog(
            text = "Manage storage is important for this app. Please grant the permission.",
            onCancel = {
                mainViewModel.saveFirstEntry()
                showWarnDialog = true
            },
            onProvide = {
                mainViewModel.saveFirstEntry()
                grantPermission()
            }
        )
    }
    else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && firstEntry.value) {
        FeatureThatRequiresReadExternalStoragePermission()
    }

    if (showWarnDialog)
        PermissionWasNotGrantedDialog(onDismiss = {showWarnDialog = false})
}

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.folder_management),
            contentDescription = null,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome!",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                navController.navigate(Routes.MainScreen.route) {
                    popUpTo(Routes.Welcome.route) { inclusive = true }
                }
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .wrapContentSize()
                .height(50.dp)
        ) {
            Text(
                color = Color.White,
                text = "GO",
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    openFile: (file: File) -> Unit,
    shareFile: (file: File) -> Unit
) {
    var path by remember { mutableStateOf("") }
    val pathsList = remember { mutableStateListOf(path) }

    var selectedType by remember { mutableStateOf(0) }
    var selectedOrder by remember { mutableStateOf(0) }

    val filesList = mainViewModel.getFiles(path, selectedType, selectedOrder)

    LaunchedEffect(true) {
        mainViewModel.compareHashes(path)
        mainViewModel.saveHashes(path)
    }
    val changedFiles = mainViewModel.changedFilesList.collectAsState()
    changedFiles.value?.forEach { Log.d("qqq", "changedFiles $it") }

    var isFilterVisible by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf<File?>(null) }
    var infoShow by remember { mutableStateOf(false) }
    var cardsNav by remember { mutableStateOf(ALL_FILES) }

    var isOverrideBackPress by remember { mutableStateOf(false) }

    val idx = pathsList.indexOf(path) - 1
    isOverrideBackPress = pathsList.indexOf(path) > 0

    BackHandler(enabled = isOverrideBackPress || editMode != null) {
        if (editMode != null)
            editMode = null
        else if (idx >= 0) {
            path = pathsList[idx]
            pathsList.removeAt(pathsList.size-1)
        }
    }

    Scaffold(
        topBar = {
            if (editMode == null)
                MainTopBar(onFilterClick = { isFilterVisible = !isFilterVisible }, isFilterVisible,
                onNavCardClick = {
                    when (it) {
                        ALL_FILES -> { cardsNav = ALL_FILES }
                        CHANGED_FILES -> { cardsNav = CHANGED_FILES }
                    }
                })
            else
                EditModeToolBar(
                    closeEditMode = { editMode = null },
                    shareClick = { editMode?.let { shareFile(it) } },
                    infoClick = { infoShow = true }
                )
         },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isFilterVisible)
                FilterView(selectedType, selectedOrder,
                    onTypeItemClick = {
                        selectedType = it
                    },
                    onOrderItemCLick = {
                        selectedOrder = it
                    }
                )
            if ((cardsNav == ALL_FILES && filesList.isNullOrEmpty()) || (cardsNav == CHANGED_FILES && changedFiles.value.isNullOrEmpty())) {
                NotFoundScreen()
            } else {
                FilesList(files = if (cardsNav == ALL_FILES) filesList!! else changedFiles.value!!, editMode = editMode,
                    onClick = { lpath, file ->
                        if (file.isFile) {
                            openFile(file)
                        }
                        else {
                            path = lpath
                            pathsList.add(path)
                        }
                    },
                    onLongClick = {
                        if (it.isFile)
                            editMode = it
                    }
                )
            }
        }
    }

    if (infoShow)
        editMode?.let { InfoFileDialog(it,
            dismiss = {
                infoShow = false
            })
        }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun FeatureThatRequiresReadExternalStoragePermission() {
    val permissionStateRead = rememberPermissionState(
        permission = Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                if(event == Lifecycle.Event.ON_START) {
                    permissionStateRead.launchPermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )
}

@Composable
fun FilesList(modifier: Modifier = Modifier,
              files: Array<out File>,
              editMode: File?,
              onClick: (path: String, file: File) -> Unit,
              onLongClick: (file: File) -> Unit) {
    LazyVerticalGrid(
        modifier = modifier.padding(8.dp),
        columns = GridCells.Adaptive(minSize = 75.dp)
    ) {
        items(files.size) { idx ->
            val file = files[idx]
            FileItem(file, editMode == file,
                onClick = {
                    onClick(it, file)
                },
                onLongClick = {
                    onLongClick(file)
                }
            )
        }
    }
}

@Composable
fun EditModeToolBar(closeEditMode: () -> Unit, shareClick: () -> Unit, infoClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { closeEditMode() }) {
            Icon(
                Icons.Default.Close,
                contentDescription = "close icon"
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd)
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Share") },
                    onClick = { shareClick() },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "share icon"
                        )
                    })
                DropdownMenuItem(
                    text = { Text("Info") },
                    onClick = { infoClick() },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = "info icon"
                        )
                    })
            }
        }
    }
}

@Composable
fun MainTopBar(
    onFilterClick: () -> Unit,
    isFilterVisible: Boolean,
    onNavCardClick: (flag: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // allocate items
        LazyRow {
            item {
                NavigationCard(title = "All Files",
                    onNavCardClick = { onNavCardClick(ALL_FILES) })
                Spacer(modifier = Modifier.width(10.dp))
                NavigationCard(title = "Changed files",
                    onNavCardClick = { onNavCardClick(CHANGED_FILES) })
            }
        }
        Image(
            modifier = Modifier
                .size(25.dp)
                .clickable { onFilterClick() },
            painter = painterResource(id = if (isFilterVisible) R.drawable.filter_reserse else R.drawable.filter),
            contentDescription = "filter image",
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
        )
    }
}

@Composable
fun NavigationCard(title: String, onNavCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .wrapContentSize()
            .padding(top = 6.dp, bottom = 6.dp)
            .clickable { onNavCardClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .align(Alignment.Center),
                textAlign = TextAlign.Center,
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItem(file: File, isEditMode: Boolean, onClick: (path: String) -> Unit, onLongClick: () -> Unit) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .combinedClickable(
                onClick = { onClick(file.absolutePath) },
                onLongClick = {
                    if (!file.isDirectory) {
                        onLongClick()
                    }
                }
            )
            .background(if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = file.findAppropriateDrawable()),
            contentDescription = "file image"
        )
        Text(
            text = file.name,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
fun FilterView(selectedType: Int, selectedOrder: Int, onTypeItemClick: (pos: Int) -> Unit, onOrderItemCLick: (pos: Int) -> Unit) {
    val itemsType = listOf("Name", "Date", "Extension", "Size")
    val itemsOrder = listOf("Descending", "Ascending")

    Column(
        modifier = Modifier
            .wrapContentSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 14.dp, start = 8.dp, end = 8.dp)
    ) {
        Text(
            text = "Sort by",
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterItems(itemsType, selectedType) {
                onTypeItemClick(it)
            }
            FilterItems(itemsOrder, selectedOrder) {
                onOrderItemCLick(it)
            }
        }
    }
}

@Composable
fun FilterItems(items: List<String>, selected: Int, onClick: (index: Int) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        items.forEachIndexed { index, item ->
            Card(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
                    .clickable {
                        onClick(index)
                    },
                colors = CardDefaults.cardColors(
                    containerColor =
                    if (selected == index) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.background
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    text = item,
                    fontSize = 14.sp,
                    color = if (selected == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                )
            }

        }
    }
}

const val ALL_FILES = "ALL_FILES"
const val CHANGED_FILES = "CHANGED_FILES"