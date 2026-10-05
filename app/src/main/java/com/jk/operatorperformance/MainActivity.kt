package com.jk.operatorperformance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.ui.OperatorPerformanceApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stateStore = ProgramStateStore(applicationContext)
        setContent { OperatorPerformanceApp(stateStore) }
    }
}
