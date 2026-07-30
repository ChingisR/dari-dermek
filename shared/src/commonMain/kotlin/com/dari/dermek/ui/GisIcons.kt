package com.dari.dermek.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Icon aliases for Compose Multiplatform compatibility.
 * Only the basic Icons.Default set ships with the runtime, so every
 * domain-specific glyph comes from [GisVectorIcons].
 */
object GisIcons {
    // Core actions
    val Search: ImageVector get() = Icons.Default.Search
    val Close: ImageVector get() = Icons.Default.Close
    val Add: ImageVector get() = Icons.Default.Add
    val Check: ImageVector get() = Icons.Default.Check
    val Delete: ImageVector get() = Icons.Default.Delete
    val Edit: ImageVector get() = Icons.Default.Edit
    @Suppress("DEPRECATION")
    val Back: ImageVector get() = Icons.AutoMirrored.Filled.ArrowBack
    val Info: ImageVector get() = Icons.Default.Info
    val Warning: ImageVector get() = Icons.Default.Warning
    val Settings: ImageVector get() = Icons.Default.Settings
    val Person: ImageVector get() = Icons.Default.Person
    val Star: ImageVector get() = Icons.Default.Star
    val Share: ImageVector get() = Icons.Default.Share
    val Email: ImageVector get() = Icons.Default.Email
    val Favorite: ImageVector get() = Icons.Default.Favorite
    val Home: ImageVector get() = Icons.Default.Home
    val Lock: ImageVector get() = Icons.Default.Lock
    val Place: ImageVector get() = Icons.Default.Place
    val Phone: ImageVector get() = Icons.Default.Phone
    val Refresh: ImageVector get() = Icons.Default.Refresh
    val MoreVert: ImageVector get() = Icons.Default.MoreVert
    val Menu: ImageVector get() = Icons.Default.Menu
    val Done: ImageVector get() = Icons.Default.Done
    val Notifications: ImageVector get() = Icons.Default.Notifications
    val Clear: ImageVector get() = Icons.Default.Clear
    val DateRange: ImageVector get() = Icons.Default.DateRange
    val Build: ImageVector get() = Icons.Default.Build
    val Face: ImageVector get() = Icons.Default.Face
    val AccountCircle: ImageVector get() = Icons.Default.AccountCircle
    val Create: ImageVector get() = Icons.Default.Create
    @Suppress("DEPRECATION")
    val Send: ImageVector get() = Icons.AutoMirrored.Filled.Send
    @Suppress("DEPRECATION")
    val ArrowForward: ImageVector get() = Icons.AutoMirrored.Filled.ArrowForward
    val ArrowDropDown: ImageVector get() = Icons.Default.ArrowDropDown
    val KeyboardArrowDown: ImageVector get() = Icons.Default.KeyboardArrowDown
    val KeyboardArrowUp: ImageVector get() = Icons.Default.KeyboardArrowUp
    val PlayArrow: ImageVector get() = Icons.Default.PlayArrow
    val ShoppingCart: ImageVector get() = Icons.Default.ShoppingCart
    val ThumbUp: ImageVector get() = Icons.Default.ThumbUp
    @Suppress("DEPRECATION")
    val ExitToApp: ImageVector get() = Icons.AutoMirrored.Filled.ExitToApp

    // Domain glyphs drawn in GisVectorIcons
    val HealthAndSafety: ImageVector get() = GisVectorIcons.ShieldCross     // Logo
    @Suppress("DEPRECATION")
    val Login: ImageVector get() = Icons.AutoMirrored.Filled.ExitToApp      // Login
    @Suppress("DEPRECATION")
    val Logout: ImageVector get() = Icons.AutoMirrored.Filled.ExitToApp     // Logout
    val Assignment: ImageVector get() = GisVectorIcons.Clipboard            // Applications
    val Medication: ImageVector get() = GisVectorIcons.Capsule              // Drugs
    @Suppress("DEPRECATION")
    val ChevronRight: ImageVector get() = Icons.AutoMirrored.Filled.ArrowForward // Navigate right
    val QrCodeScanner: ImageVector get() = GisVectorIcons.QrCode            // QR scanner
    val Schedule: ImageVector get() = GisVectorIcons.Clock                  // Timeline
    val Pause: ImageVector get() = Icons.Default.Clear                      // Clock paused
    val Vaccines: ImageVector get() = GisVectorIcons.Syringe                // Immunological
    val Biotech: ImageVector get() = GisVectorIcons.Flask                   // Lab
    val CleaningServices: ImageVector get() = GisVectorIcons.Spray          // Disinfectant
    val Restaurant: ImageVector get() = GisVectorIcons.FeedBowl             // Feed additive
    val Gavel: ImageVector get() = GisVectorIcons.Gavel                     // Committee
    val Science: ImageVector get() = GisVectorIcons.Flask                   // Expert
    val LocalShipping: ImageVector get() = GisVectorIcons.Truck             // Border
    val Inventory: ImageVector get() = GisVectorIcons.Package               // Warehouse
    val Receipt: ImageVector get() = GisVectorIcons.Receipt                 // Prescriptions
    val Assessment: ImageVector get() = GisVectorIcons.BarChart             // Admin
    val Business: ImageVector get() = GisVectorIcons.Building               // Applicant
    val AccountBalance: ImageVector get() = GisVectorIcons.ShieldCross      // Committee org
    val Security: ImageVector get() = GisVectorIcons.ShieldCheck            // Border inspector
    val Warehouse: ImageVector get() = GisVectorIcons.Warehouse             // Warehouse
    val Agriculture: ImageVector get() = GisVectorIcons.Leaf                // Farmer
    val AdminPanelSettings: ImageVector get() = Icons.Default.Settings      // Admin
    val SearchOff: ImageVector get() = Icons.Default.Search                 // No results
    val Inbox: ImageVector get() = GisVectorIcons.Package                   // Empty list
    val VerifiedUser: ImageVector get() = GisVectorIcons.ShieldCheck        // Verified
    val GppBad: ImageVector get() = Icons.Default.Warning                   // Counterfeit
    val Pharmacovigilance: ImageVector get() = GisVectorIcons.ShieldHeart   // Safety monitoring
    val Recycle: ImageVector get() = GisVectorIcons.Recycle                 // Disposal
}
