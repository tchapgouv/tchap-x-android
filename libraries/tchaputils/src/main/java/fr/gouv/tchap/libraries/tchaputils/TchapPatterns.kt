/*
 * MIT License
 *
 * Copyright (c) 2024. DINUM
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.gouv.tchap.libraries.tchaputils

import java.util.Locale

object TchapPatterns {

    /**
     * Get the homeserver name of a matrix identifier.
     * The identifier type may be any matrix identifier type: user id, room id, ...
     * For example in case of "@jean-philippe.martin-modernisation.fr:matrix.test.org", this will return "matrix.test.org".
     * in case of "!AAAAAAA:matrix.test.org", this will return "matrix.test.org".
     *
     * @param mxId the matrix identifier.
     * @return the homeserver name, if any.
     */
    fun getHomeserverNameFromMxId(mxId: String): String = mxId.substringAfter(":", "")

    /**
     * Get the Tchap display name of the homeserver mentioned in a matrix identifier.
     * The identifier type may be any matrix identifier type: user id, room id, ...
     * The returned name is capitalize.
     * The Tchap HS display name is the component mentioned before the suffix "tchap.gouv.fr"
     * For example in case of "@jean-philippe.martin-modernisation.fr:name1.tchap.gouv.fr", this will return "Name1".
     * in case of "@jean-philippe.martin-modernisation.fr:agent.name2.tchap.gouv.fr", this will return "Name2".
     *
     * @param mxId the matrix identifier.
     * @return the Tchap display name of the homeserver.
     */
    fun getHomeserverDisplayNameFromMxId(mxId: String): String {
        var homeserverName = getHomeserverNameFromMxId(mxId)
        if (homeserverName.contains("tchap.gouv.fr")) {
            homeserverName.split("\\.".toRegex()).let {
                if (it.size >= 4) homeserverName = it[it.size - 4]
            }
        }
        return homeserverName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    /**
     * Tells whether a homeserver name corresponds to an external server or not.
     *
     * @param homeServerName the homeserver name to check.
     * @return true if external.
     */
     fun String.isExternalTchapServer() = this.isEmpty() || this.startsWith("e.") || this.startsWith("agent.externe.")

    /**
     * Get name part of a display name by removing the domain part if any.
     * For example in case of "Jean Martin `[Modernisation]`", this will return "Jean Martin".
     *
     * @param displayName the display name to compute.
     * @return displayName without domain (or the display name itself if no domain has been found).
     */
    fun getNameFromDisplayName(displayName: String): String {
        return displayName.split(DISPLAY_NAME_FIRST_DELIMITER)
            .first()
            .trim()
    }

    /**
     * Get the room name from a display name according to the given room type.
     * In the case of a direct message, it will remove the domain part of the display name.
     * For example in case of "Jean Martin `[Modernisation]`", this will return "Jean Martin".
     *
     * Otherwise, it will keep the initial name.
     *
     * @param displayName the display name to compute.
     * @param roomType the room type associated with the given display name.
     *
     * @return displayName without domain (or the display name itself if the room is not a DM).
     */
//    fun getRoomNameFromDisplayName(displayName: String, roomType: TchapRoomType): String {
//        return when (roomType) {
//            TchapRoomType.DIRECT -> getNameFromDisplayName(displayName)
//            else                 -> displayName
//        }
//    }

    /**
     * Get the potential domain name from a display name.
     * For example in case of "Jean Martin `[Modernisation]`", this will return "Modernisation".
     *
     * @param displayName the display name to compute.
     * @return displayName without name, empty string if no domain is available.
     */
    fun getDomainFromDisplayName(displayName: String): String {
        return displayName.split(DISPLAY_NAME_FIRST_DELIMITER)
            .elementAtOrNull(1)
            ?.split(DISPLAY_NAME_SECOND_DELIMITER)
            ?.first()
            ?.trim()
            ?: DEFAULT_EMPTY_STRING
    }

    /**
     * Build a display name from the tchap user identifier.
     * We don't extract the domain for the moment in order to not display unexpected information.
     * For example in case of "@jean-philippe.martin-modernisation.fr:matrix.org", this will return "Jean-Philippe Martin".
     * Note: in case of an external user identifier, we return the local part of the id which corresponds to their email.
     *
     * @return displayName without domain, or null if the user identifier is not valid.
     */
    fun String.toDisplayName(): String {
        // Extract identifier from user ID.
        val identifier = this.substringAfter('@').substringBefore(':')
        val lastHyphenIndex = identifier.lastIndexOf('-')

        // Return the identifier as-is if no transformations were needed.
        if (lastHyphenIndex == -1) return identifier

        return if (this.isExternalTchapUser()) {
            // Handle external Tchap user case: replace single hyphen with '@'.
            if (identifier.indexOf('-') == lastHyphenIndex) {
                identifier.replaceRange(lastHyphenIndex..lastHyphenIndex, "@")
            } else identifier
        } else {
            // Handle internal user case.
            buildString {
                var capitalizeNext = true
                for (i in 0 until lastHyphenIndex) {
                    val char = identifier[i]
                    when {
                        (capitalizeNext && (char == '.' || char == '-')) -> continue
                        char == '.' -> {
                            // Replace the dot character by space character
                            append(' ')
                            capitalizeNext = true
                        }
                        char == '-' -> {
                            append(char)
                            capitalizeNext = true
                        }
                        capitalizeNext -> {
                            append(char.uppercaseChar())
                            capitalizeNext = false
                        }
                        else -> append(char)
                    }
                }
            }
        }
    }

    /**
     * Tells whether the provided tchap identifier corresponds to an extern user.
     * Note: invalid tchap identifier will be considered as external.
     *
     * @param tchapUserId user identifier (ie. the matrix identifier).
     * @return true if external.
     */
    fun String.isExternalTchapUser(): Boolean {
        val homeServerName = getHomeserverNameFromMxId(this)
        return homeServerName.isExternalTchapServer()
    }

    /**
     * Create a room alias name with a prefix.
     *
     * @param prefix the alias name prefix.
     * @return the suggested alias name.
     */
    fun createRoomAliasName(prefix: String): String {
        return prefix.trim().replace("[^a-zA-Z0-9]".toRegex(), "") + getRandomString(10)
    }

    /**
     * Create a room alias with a prefix.
     *
     * @param session the user's session.
     * @param prefix the alias name prefix.
     * @return the suggested alias.
     */
    fun createRoomAlias(sessionId: String, prefix: String): String {
        return "#${createRoomAliasName(prefix)}:${getHomeserverNameFromMxId(sessionId)}"
    }

    /**
     * Extract the local part of the given room alias.
     *
     * @param roomAlias the room alias to parse.
     * @return the alias local part.
     */
    fun extractRoomAliasName(roomAlias: String): String {
        return roomAlias.substringAfter("#").substringBefore(":")
    }

    /**
     * Generate a random string of the given number of characters.
     *
     * @param length the random string length.
     * @return the resulting random string.
     */
    fun getRandomString(length: Int): String {
        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length).map { charPool.random() }.joinToString("")
    }

    private const val DISPLAY_NAME_FIRST_DELIMITER = "["
    private const val DISPLAY_NAME_SECOND_DELIMITER = "]"
    private const val DEFAULT_EMPTY_STRING = ""
}
