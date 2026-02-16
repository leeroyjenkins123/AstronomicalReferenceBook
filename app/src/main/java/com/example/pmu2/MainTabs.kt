package com.example.pmu2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MainTabs(viewModel: NewsViewModel, modifier: Modifier = Modifier) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // ВАЖНО: состояние InfoScreen теперь здесь
    var showInfo by rememberSaveable { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf("Новости", "3D галактика")

    Column(modifier = modifier.fillMaxSize()) {

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> FourQuartersScreen(
                viewModel = viewModel,
                getDisplayLikes = { news -> viewModel.getDisplayLikes(news) },
                onToggleLike = { news -> viewModel.toggleLike(news) },
                modifier = Modifier.weight(1f)
            )

            1 -> OpenGLScreen(
                modifier = Modifier.weight(1f),
                showInfo = showInfo,
                selectedIndex = selectedIndex,
                onShowInfo = { idx ->
                    selectedIndex = idx
                    showInfo = true
                },
                onHideInfo = {
                    showInfo = false
                },
                onIndexChanged = { newIndex ->
                    selectedIndex = newIndex                 // сохраняем при смене
                }
            )
        }
    }
}

@Composable
fun OpenGLScreen(
    modifier: Modifier = Modifier,
    showInfo: Boolean,
    selectedIndex: Int,
    onShowInfo: (Int) -> Unit,
    onHideInfo: () -> Unit,
    onIndexChanged: (Int) -> Unit
) {
    val context = LocalContext.current // сохраняем OpenGLView между пересозданиями
    val glView = remember { OpenGLView(context,selectedIndex) }


    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. OpenGLView — ВСЕГДА на экране
        Column(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { glView
                },
                modifier = Modifier
                    .weight(9f)
                    .fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    glView.getRenderer().selectPrev()
                    val newIdx = glView.getRenderer().getSelectedIndex()
                    glView.getRenderer().setSelectedIndex(newIdx)
                    onIndexChanged(newIdx)
                }) {
                    Text("Влево")
                }

                Button(onClick = {
                    onShowInfo(glView.getRenderer().getSelectedIndex())
                }) {
                    Text("Инфо")
                }

                Button(onClick = {
                    glView.getRenderer().selectNext()
                    val newIdx = glView.getRenderer().getSelectedIndex()
                    glView.getRenderer().setSelectedIndex(newIdx)
                    onIndexChanged(newIdx)

                }) {
                    Text("Вправо")
                }
            }
        }

        // 2. InfoScreen — поверх, не убирая OpenGLView
        if (showInfo) {
            InfoScreen(
                selectedIndex = selectedIndex,
                onBack = {
                    onHideInfo()
                    glView.getRenderer().setSelectedIndex(selectedIndex)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
            )
        }
    }
}

@Composable
fun InfoScreen(
    selectedIndex: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (selectedIndex) {
        0 -> "Солнце"
        1 -> "Меркурий"
        2 -> "Венера"
        3 -> "Земля"
        4 -> "Луна"
        5 -> "Марс"
        6 -> "Юпитер"
        7 -> "Сатурн"
        8 -> "Уран"
        9 -> "Нептун"
        else -> "Объект"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("Назад")
            }
            Text(
                text = title,
                color = Color.White
            )
            Spacer(Modifier.width(80.dp))
        }

        AndroidView(
            factory = { ctx ->
                OpenGLInfoView(ctx, selectedIndex)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(7f)
        )

        Row(
            modifier = Modifier
                .weight(3f)
                .fillMaxWidth()
                .background(Color.Black)
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = PlanetDescription.getDescription(selectedIndex),
                color = Color.White,
                fontSize = 15.sp
            )
        }
    }
}
