package com.fairsplit.ui.theme

import androidx.compose.ui.unit.dp

/**
 * FairSplit Spacing System
 * Based on 8dp grid system from design analysis
 */
object Spacing {
    // Core 8dp Grid System
    val none = 0.dp
    val xxxs = 2.dp    // Half step
    val xxs = 4.dp     // Quarter grid
    val xs = 8.dp      // Base unit
    val sm = 12.dp     // 1.5x base
    val md = 16.dp     // 2x base
    val lg = 24.dp     // 3x base
    val xl = 32.dp     // 4x base
    val xxl = 40.dp    // 5x base
    val xxxl = 48.dp   // 6x base
    val xxxxl = 64.dp  // 8x base
    
    // Screen-Level Spacing
    val screenHorizontal = 16.dp  // Standard screen side padding
    val screenVertical = 16.dp    // Standard screen top/bottom padding
    val screenPadding = 16.dp     // Unified screen padding
    
    // Component-Specific Heights
    val buttonHeight = 56.dp      // Standard button height (h-14 from designs)
    val inputHeight = 56.dp       // Input field height (h-14 from designs)
    val chipHeight = 36.dp        // Filter chip height
    val iconSize = 24.dp          // Standard icon size
    val iconSizeLarge = 48.dp     // Large icon size (dashboard icons)
    val iconSizeSmall = 20.dp     // Small icon size
    val avatarSize = 40.dp        // User avatar size
    val logoSize = 96.dp          // Logo container size (h-24 w-24 = 96dp)
    
    // Card Spacing
    val cardPadding = 16.dp              // Standard card padding
    val cardPaddingLarge = 24.dp         // Large card padding
    val cardPaddingSmall = 12.dp         // Small card padding
    val cardSpacing = 12.dp              // Gap between cards
    val cardElevation = 4.dp             // Card shadow elevation
    
    // List Item Spacing
    val listItemPadding = 16.dp          // List item internal padding
    val listItemSpacing = 12.dp          // Gap between list items
    val listItemHeight = 72.dp           // Standard list item height
    val listItemHeightSmall = 56.dp      // Compact list item height
    
    // Input Field Spacing
    val inputPadding = 16.dp             // Input field internal padding
    val inputPaddingStart = 48.dp        // Left padding for icon (pl-12 = 48dp)
    val inputBorderWidth = 1.dp          // Input border width
    val inputSpacing = 16.dp             // Gap between input fields
    
    // Button Spacing
    val buttonPadding = 16.dp            // Button internal padding
    val buttonSpacing = 12.dp            // Gap between buttons
    val buttonIconSpacing = 8.dp         // Gap between icon and text in button
    
    // Icon Container Spacing
    val iconContainerSize = 48.dp        // Icon container size (dashboard)
    val iconContainerSizeSmall = 40.dp   // Small icon container
    val iconContainerSizeLarge = 64.dp   // Large icon container
    val iconContainerPadding = 12.dp     // Icon container internal padding
    
    // Grid Spacing
    val gridSpacing = 12.dp              // Gap between grid items (gap-3 from designs)
    val gridSpacingSmall = 8.dp          // Small grid gap
    val gridSpacingLarge = 16.dp         // Large grid gap
    
    // Section Spacing
    val sectionSpacing = 24.dp           // Gap between major sections
    val sectionHeaderSpacing = 16.dp     // Gap between section header and content
    val sectionTitleSpacing = 12.dp      // Gap between title and subtitle
    
    // Divider Spacing
    val dividerThickness = 1.dp          // Standard divider height
    val dividerSpacing = 16.dp           // Vertical space around dividers
    
    // Bottom Sheet Spacing
    val bottomSheetPadding = 24.dp       // Bottom sheet internal padding
    val bottomSheetHandleWidth = 32.dp   // Drag handle width
    val bottomSheetHandleHeight = 4.dp   // Drag handle height
    
    // Dialog Spacing
    val dialogPadding = 24.dp            // Dialog internal padding
    val dialogButtonSpacing = 8.dp       // Gap between dialog buttons
    val dialogContentSpacing = 16.dp     // Gap between dialog content elements
    
    // Top App Bar Spacing
    val appBarHeight = 64.dp             // App bar height
    val appBarPadding = 16.dp            // App bar horizontal padding
    val appBarElevation = 0.dp           // App bar elevation (designs use shadow instead)
    
    // Bottom Navigation Spacing
    val bottomNavHeight = 80.dp          // Bottom navigation height
    val bottomNavPadding = 8.dp          // Bottom navigation padding
    
    // Chip Spacing
    val chipPadding = 12.dp              // Chip internal horizontal padding
    val chipSpacing = 8.dp               // Gap between chips
    val chipIconSpacing = 4.dp           // Gap between chip icon and text
    
    // Badge Spacing
    val badgeSize = 20.dp                // Badge diameter
    val badgePadding = 4.dp              // Badge internal padding
    
    // Shadow/Elevation
    val elevationNone = 0.dp
    val elevationSmall = 2.dp
    val elevationMedium = 4.dp
    val elevationLarge = 8.dp
    val elevationExtraLarge = 12.dp
    
    // Border Width
    val borderWidthThin = 0.5.dp
    val borderWidthDefault = 1.dp
    val borderWidthThick = 2.dp
    
    // Specific Component Dimensions (from design analysis)
    
    // Add Expense Screen
    val numericKeypadHeight = 320.dp     // Numeric keypad container height
    val numericKeypadButtonSize = 64.dp  // Individual number button size
    val categoryGridItemMinWidth = 100.dp // Category chip min width
    
    // Dashboard
    val balanceCardHeight = 120.dp       // Balance card height (approximate)
    val summaryCardHeight = 100.dp       // Summary card height (2x2 grid)
    val chartHeight = 200.dp             // Chart placeholder height
    
    // List Screens
    val receiptImageSize = 48.dp         // Receipt image size (size-12 = 48dp)
    val swipeActionWidth = 80.dp         // Swipe action button width
    
    // Auth Screens
    val logoContainerSize = 96.dp        // Logo circle size (h-24 w-24)
    val socialButtonHeight = 48.dp       // Social auth button height
    val orDividerSpacing = 24.dp         // Space around "OR" divider
    
    // Settings Screen
    val settingsItemHeight = 56.dp       // Settings list item height
    val profileImageSize = 80.dp         // Profile image size
}

/**
 * Responsive Spacing Extensions
 * Can be used for different screen sizes if needed
 */
object ResponsiveSpacing {
    // Small devices (phones in portrait)
    object Compact {
        val screenPadding = 12.dp
        val cardPadding = 12.dp
        val sectionSpacing = 16.dp
    }
    
    // Medium devices (phones in landscape, small tablets)
    object Medium {
        val screenPadding = 16.dp
        val cardPadding = 16.dp
        val sectionSpacing = 24.dp
    }
    
    // Large devices (tablets)
    object Expanded {
        val screenPadding = 24.dp
        val cardPadding = 24.dp
        val sectionSpacing = 32.dp
    }
}
