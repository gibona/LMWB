package com.anobig.lmwb.bluetooth.placeholder

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 */
object PlaceholderContent {

    /**
     * A placeholder item representing a piece of content.
     */
    data class PlaceholderItem(val id: Int, val name: String, val address: String) {
        override fun toString(): String = name
    }
}