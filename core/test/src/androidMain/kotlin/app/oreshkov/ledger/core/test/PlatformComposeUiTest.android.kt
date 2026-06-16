package app.oreshkov.ledger.core.test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36]) // Robolectric shadow support for API 37 is not yet ready
actual abstract class PlatformComposeUiTest actual constructor()