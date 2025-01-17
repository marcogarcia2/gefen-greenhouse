package com.example.gefen_greenhouse

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

// Função que traduz o número do mês em seu nome
fun getMonthString(number: Int): String {
    return when (number) {
        1 -> "Janeiro"
        2 -> "Fevereiro"
        3 -> "Março"
        4 -> "Abril"
        5 -> "Maio"
        6 -> "Junho"
        7 -> "Julho"
        8 -> "Agosto"
        9 -> "Setembro"
        10 -> "Outubro"
        11 -> "Novembro"
        12 -> "Dezembro"
        else -> "Número inválido"
    }
}

// Traduz uma cor dependendo da string
fun getColorForString(stringId: Int): Int {
    return when (stringId) {
        R.string.sucesso -> Color.parseColor("#4BAE4F") // Verde
        R.string.falhou -> Color.parseColor("#FF4141")  // Vermelho
        R.string.aguardando -> Color.parseColor("#ABABAB") // Cinza
        R.string.indeterminado -> Color.parseColor("#245DD9")
        else -> Color.BLACK // Cor padrão
    }
}

// Retorna o texto no formato correto com a cor desejada
fun getColoredText(context: Context, stringId: Int): SpannableString {
    val text = context.getString(stringId)
    val color = getColorForString(stringId) // Obtém a cor automaticamente
    val spannable = SpannableString(text)
    spannable.setSpan(
        ForegroundColorSpan(color),
        0,
        text.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return spannable
}
