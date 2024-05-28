/*
 *  References:
 *     https://github.com/eozsahin1993/Accordion-Menu-Example/tree/main
 *     https://medium.com/@eozsahin1993/accordion-menu-in-jetpack-compose-32151adf6d80
 */

package com.example.businesshours.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.businesshours.ui.theme.*

data class AccordionModel(val header: String, val rows: List<Row>) {
    data class Row(val accordionDayOfWeek: String, val accordionTimeWindow: String)
}

@Composable
fun AccordionGroup(modifier: Modifier = Modifier, group: List<AccordionModel>) {
    Column(modifier = modifier) { group.forEach { Accordion(model = it) } }
}

@Composable
fun Accordion(modifier: Modifier = Modifier, model: AccordionModel) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        AccordionHeader(title = model.header, isExpanded = expanded) { expanded = !expanded }
        AnimatedVisibility(visible = expanded) {
            Surface(
                color = BlueGray50,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                LazyColumn {
                    items(model.rows) { row ->
                        AccordionRow(row)
                        Divider(thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AccordionHeader(
    title: String = "Header",
    isExpanded: Boolean = false,
    onTapped: () -> Unit = {}
) {
    val degrees = if (isExpanded) 180f else 0f

    Surface(
        color = BlueGray50,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.clickable { onTapped() }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, Modifier.weight(1f), onTextLayout = {})
            Surface(shape = CircleShape) {
                Icon(
                    Icons.Outlined.ArrowDropDown,
                    contentDescription = "arrow-down",
                    modifier = Modifier.rotate(degrees),
                    tint = PurpleGrey40
                )
            }
        }
    }
}

@Preview
@Composable
private fun AccordionRow(model: AccordionModel.Row = AccordionModel.Row("AAPL", "$328.89")) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
        Text(model.accordionDayOfWeek, Modifier.weight(1f), onTextLayout = {})
        Surface(shape = RoundedCornerShape(8.dp)) {
            Text(
                text = model.accordionTimeWindow,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                color = Green500,
                onTextLayout = {}
            )
        }
    }
}
