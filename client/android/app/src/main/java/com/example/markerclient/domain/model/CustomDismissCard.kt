package com.example.markerclient.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Stable
class CustomDismissCard {
    var cardVisible by mutableStateOf(getDefaultCardVisible())
        private set
    var cardModifier by mutableStateOf(getDefaultCardModifier())
        private set
    var cardContent by mutableStateOf(getDefaultCardContent())
        private set
    var leftToRightDismiss by mutableStateOf(getDefaultLeftToRightDismiss())
        private set
    var rightToLeftDismiss by mutableStateOf(getDefaultRightToLeftDismiss())
        private set
    var cardOnDismiss by mutableStateOf(getDefaultCardOnDismiss())
        private set

    fun update(visible : Boolean = cardVisible,
               modifier : Modifier = cardModifier,
               content : @Composable () -> Unit = cardContent,
               ltrDismiss : Boolean = leftToRightDismiss,
               rtlDismiss : Boolean = rightToLeftDismiss,
               onDismiss : () -> Unit = cardOnDismiss
    ) : CustomDismissCard {
        cardVisible = visible
        cardModifier = modifier
        cardContent = content
        leftToRightDismiss = ltrDismiss
        rightToLeftDismiss = rtlDismiss
        cardOnDismiss = onDismiss

        return this
    }

    fun reset() {
        cardVisible = getDefaultCardVisible()
        cardModifier = getDefaultCardModifier()
        cardContent = getDefaultCardContent()
        leftToRightDismiss = getDefaultLeftToRightDismiss()
        rightToLeftDismiss = getDefaultRightToLeftDismiss()
        cardOnDismiss = getDefaultCardOnDismiss()
    }

    companion object {
        private val DEFAULT_CARD_VISIBLE : Boolean = false
        private val DEFAULT_CARD_MODIFIER : Modifier = Modifier
        private val DEFAULT_CARD_CONTENT : @Composable (() -> Unit) = @Composable {}
        private val DEFAULT_LEFT_TO_RIGHT_DISMISS : Boolean = true
        private val DEFAULT_RIGHT_TO_LEFT_DISMISS : Boolean = true
        private val DEFAULT_CARD_ON_DISMISS : () -> Unit = {}

        fun getDefaultCardVisible() : Boolean {
            return DEFAULT_CARD_VISIBLE
        }

        fun getDefaultCardModifier() : Modifier {
            return DEFAULT_CARD_MODIFIER
        }

        fun getDefaultCardContent() : @Composable (() -> Unit) {
            return DEFAULT_CARD_CONTENT
        }

        fun getDefaultLeftToRightDismiss() : Boolean {
            return DEFAULT_LEFT_TO_RIGHT_DISMISS
        }

        fun getDefaultRightToLeftDismiss() : Boolean {
            return DEFAULT_RIGHT_TO_LEFT_DISMISS
        }

        fun getDefaultCardOnDismiss() : () -> Unit  {
            return DEFAULT_CARD_ON_DISMISS
        }
    }
}