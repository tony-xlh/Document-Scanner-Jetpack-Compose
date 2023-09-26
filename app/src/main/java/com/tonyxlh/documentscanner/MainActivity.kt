package com.tonyxlh.documentscanner

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tonyxlh.documentscanner.ui.theme.DocumentScannerTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocumentScannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            TopAppBar(
                                title = {
                                    Text("Document Scanner")
                                },
                            )
                            sampleDocuments().forEach { doc ->
                                DocumentItem(doc)
                            }
                        }
                        FloatingActionButton(
                            modifier = Modifier
                                .padding(all = 16.dp)
                                .align(alignment = Alignment.BottomEnd),
                            onClick = {
                                Log.d("DBR","clicked");
                            }
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentItem(doc: Document) {
    Row(modifier = Modifier
        .padding(all = 8.dp)
        .fillMaxWidth()
        .clickable(onClick = {
           Log.d("DBR","item clicked");
        })) {
        Image(
            painter = painterResource(R.drawable.thumbnail),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = doc.name,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = doc.date.toString())
        }
    }
}

fun sampleDocuments(): MutableList<Document> {
    val docs = mutableListOf<Document>();
    docs.add(Document(1695714698605,"Document 1"));
    docs.add(Document(1695714799615,"Document 2"));
    return docs
}