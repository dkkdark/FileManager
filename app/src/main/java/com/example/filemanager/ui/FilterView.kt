package com.example.filemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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