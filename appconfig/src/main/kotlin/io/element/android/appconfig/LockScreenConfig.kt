/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.appconfig

object LockScreenConfig {

    /**
     * Whether the PIN is mandatory or not.
     */
    const val IS_PIN_MANDATORY: Boolean = true

    /**
     * Some PINs are blacklisted.
     */
    val PIN_BLACKLIST = setOf("0000", "1234")

    /**
     * The size of the PIN.
     */
    const val PIN_SIZE = 4

    /**
     * Number of attempts before the user is logged out.
     */
    const val MAX_PIN_CODE_ATTEMPTS_NUMBER_BEFORE_LOGOUT = 3

    /**
     * Time period before locking the app once backgrounded.
     */
    const val GRACE_PERIOD_IN_MILLIS = 90 * 1000L
}
