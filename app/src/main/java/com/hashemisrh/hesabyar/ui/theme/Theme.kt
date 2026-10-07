package com.hashemisrh.hesabyar.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hashemisrh.hesabyar.MainActivity

private val LightColors=lightColorScheme(primary=Color(0xFF0F5C5A),onPrimary=Color.White,secondary=Color(0xFFC7A86B),background=Color(0xFFF5F3EE),surface=Color.White,onBackground=Color(0xFF102A2A),onSurface=Color(0xFF102A2A))
private val DarkColors=darkColorScheme(primary=Color(0xFF4CB3A5),onPrimary=Color(0xFF062C2B),secondary=Color(0xFFC7A86B),background=Color(0xFF102A2A),surface=Color(0xFF163636),onBackground=Color(0xFFF5F3EE),onSurface=Color(0xFFF5F3EE))
@Composable fun HesabYarTheme(content:@Composable()->Unit){MaterialTheme(colorScheme=LightColors,typography=Typography(),content=content)}
