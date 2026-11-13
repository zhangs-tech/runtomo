package com.zhangs_tech.runtomo

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/** Host activity for Compose UI tests that need Hilt injection. Debug builds only. */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
