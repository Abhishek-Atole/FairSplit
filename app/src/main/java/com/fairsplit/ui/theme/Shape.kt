package com.fairsplit.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// FairSplit Shape System
val FairSplitShapes = Shapes(
    // Extra Small - For small chips, badges
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small - For small buttons, tags
    small = RoundedCornerShape(8.dp),
    
    // Medium - For input fields, regular buttons, cards
    medium = RoundedCornerShape(12.dp),
    
    // Large - For larger cards, bottom sheets
    large = RoundedCornerShape(16.dp),
    
    // Extra Large - For modal dialogs, bottom sheets
    extraLarge = RoundedCornerShape(24.dp)
)

// Custom Shape Extensions
object CustomShapes {
    // Input Fields (rounded-lg from designs)
    val InputField = RoundedCornerShape(12.dp)
    
    // Primary Buttons (rounded-lg from designs)
    val Button = RoundedCornerShape(12.dp)
    
    // Cards (rounded-xl from designs)
    val Card = RoundedCornerShape(16.dp)
    
    // Large Cards (rounded-2xl from designs)
    val CardLarge = RoundedCornerShape(24.dp)
    
    // Small Cards (rounded-lg)
    val CardSmall = RoundedCornerShape(12.dp)
    
    // Chips (rounded-full from designs)
    val Chip = CircleShape
    
    // Category Chips (rounded-full from designs)
    val CategoryChip = CircleShape
    
    // Filter Chips (rounded-full from designs)
    val FilterChip = CircleShape
    
    // FAB (Floating Action Button)
    val FAB = CircleShape
    
    // Avatar (rounded-full)
    val Avatar = CircleShape
    
    // Badge (rounded-full)
    val Badge = CircleShape
    
    // Bottom Sheet (rounded top corners only)
    val BottomSheet = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    // Bottom Navigation (rounded top corners)
    val BottomNavigation = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    // Numeric Keypad Container (rounded top corners)
    val NumericKeypad = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    // Dialog (rounded all corners)
    val Dialog = RoundedCornerShape(16.dp)
    
    // Logo Container (circle from splash screen)
    val LogoContainer = CircleShape
    
    // Social Auth Buttons (rounded-lg)
    val SocialButton = RoundedCornerShape(12.dp)
    
    // Outlined Button (rounded-lg)
    val OutlinedButton = RoundedCornerShape(12.dp)
    
    // Text Button (rounded-lg)
    val TextButton = RoundedCornerShape(8.dp)
    
    // Search Bar (rounded-full)
    val SearchBar = CircleShape
    
    // Progress Bar (rounded-full)
    val ProgressBar = CircleShape
    
    // Segmented Control (rounded-lg)
    val SegmentedControl = RoundedCornerShape(12.dp)
    
    // Segmented Control Button (rounded-lg)
    val SegmentedControlButton = RoundedCornerShape(8.dp)
    
    // Summary Card (rounded-xl)
    val SummaryCard = RoundedCornerShape(16.dp)
    
    // Balance Card (rounded-xl)
    val BalanceCard = RoundedCornerShape(16.dp)
    
    // Icon Container (rounded-lg)
    val IconContainer = RoundedCornerShape(12.dp)
    
    // Small Icon Container (rounded)
    val IconContainerSmall = RoundedCornerShape(8.dp)
    
    // Receipt Image (rounded from expense list)
    val ReceiptImage = RoundedCornerShape(8.dp)
}

// Shape Sizes (for consistent corner radius usage)
object ShapeTokens {
    val None = 0.dp
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val Full = 9999.dp // Effectively creates a pill/circle shape
}
