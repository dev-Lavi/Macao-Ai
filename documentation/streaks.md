# Daily Streak, Progress & Fluency Score System (MacaoAI)

## 1. Overview & Objectives
The Daily Streak & Learning Tracker system provides users with immediate visual feedback on their language learning consistency, total experience points (XP), weekly momentum, and overall fluency score.

This feature maps directly to the **Analytics** tab (`HomeTab.Chart`) in the bottom navigation bar and delivers:
1. **Interactive Date Bar**: Horizontal date selector highlighting "Today" with adjacent past and upcoming dates.
2. **Weekly Momentum Card**: Highlighting active streak days with connected pill backgrounds and the `fire.xml` icon.
3. **Hero XP & Metrics**: Prominently featuring Total Experience (XP), completed Lessons count, Minutes Studied, and Points.
4. **Learning Tracker Cards**:
   - **Daily Streak Card**: Displays current streak vs annual target (e.g. `31/365`) alongside a 5-week contribution heatmap dot grid.
   - **Progress Card**: Displays current trajectory (`Improving`, `Consistent`, `Mastering`) with a dynamic 7-bar rounded column chart.
   - **Fluency Score Card**: Displays calculated language fluency percentage (e.g. `78% - Intermediate`) with speaking, listening, accuracy, and vocabulary breakdown.

---

## 2. Visual Design System

### 2.1 Color Palette
| Token Name | Hex Code | Purpose |
|---|---|---|
| `AppBackground` | `#FBF8F5` | Primary screen canvas background |
| `CardBackground` | `#FFFFFF` | Weekly Streak white elevated card |
| `CaramelCard` | `#A5775B` | Learning Tracker card background (Daily Streak, Progress, Fluency) |
| `CaramelCardDark` | `#8F644A` | Heatmap active dots / darker tone |
| `PeachPill` | `#FCD5CB` | "Today" date pill and connected active streak pill |
| `PeachPillBorder` | `#F5C2B5` | Border for active streak range |
| `FireIconColor` | `#926247` | Active streak day icon tint |
| `InactiveDayCircle` | `#E3DED9` | Inactive / future day circle outline |
| `TextPrimary` | `#322722` (`MindfulBrown`) | Primary titles and large metrics |
| `TextSecondary` | `#7E6E66` (`MindfulBrown60`) | Labels and subtitles |
| `TextWhite` | `#FFFFFF` | Text and icons inside Caramel cards |
| `TextWhiteMuted` | `rgba(255, 255, 255, 0.7)` | Subtitles inside Caramel cards |

### 2.2 Typography
- **Headings & Hero Metrics**: `Inter` / `ClashGrotesk` Bold (32sp - 40sp for `1883 XP`, 24sp for section titles, 28sp for tracker numbers).
- **Body & Labels**: `Inter` SemiBold (12sp - 14sp for labels, 10sp for day captions).

### 2.3 Assets
- **Fire Icon**: `MacaoAI/app/src/main/res/drawable/fire.xml` (used for active streak badges, daily streak card header, and milestone indicators).
- **Back Button**: `MacaoAI/app/src/main/res/drawable/back.xml` inside a circular 42dp white card with border.
- **Chart Tab**: `MacaoAI/app/src/main/res/drawable/chart.xml` in `BottomNavBar`.

---

## 3. UI Component Hierarchy

```
AnalyticsScreen
├── TopBar
│   ├── Circular Back Button (<)
│   └── "Analytics" Title
├── DateSelectorBar
│   └── Horizontal row: [... Day - 2, Day - 1, [Today, 8 Jul], Day + 1, Day + 2 ...]
├── WeeklyStreakCard (White, 24dp rounded, shadow)
│   ├── Day Columns (Mo, Tu, We, Th, Fr, Sa, Su)
│   │   ├── Contiguous active streak connected by PeachPill
│   │   └── Circular fire badge (fire.xml) or hollow circle
│   └── Footer Row
│       ├── Left: fire.xml + "12 days"
│       └── Right: Milestone / Streak Freeze badges
├── HeroStatsSection
│   ├── Total XP ("1883 XP")
│   ├── "Total Experience" Label
│   └── 3-Column Metrics (Lessons, Minutes Studies, Points)
├── SectionHeader: "Learning Tracker"
├── LearningTrackerGrid (CaramelCards)
│   ├── DailyStreakCard
│   │   ├── Header: fire.xml + "Daily Streak"
│   │   ├── Metric: "31/365"
│   │   └── Activity Heatmap Grid (5 weeks x 7 days dot matrix)
│   ├── ProgressCard
│   │   ├── Header: "Progress"
│   │   ├── Status: "Improving"
│   │   └── Vertical Bar Chart (7 rounded white bars)
│   └── FluencyScoreCard
│       ├── Header: "Fluency Score"
│       ├── Score: "82%" (Intermediate)
│       └── Competency breakdown (Speaking, Accuracy, Vocabulary)
└── BottomNavBar (Pill-shaped, HomeTab.Chart highlighted)
```

---

## 4. Backend Architecture & Calculations

### 4.1 Firestore Schema Additions (`users/{uid}/progress/{languageCode}`)
```typescript
interface UserProgressDoc {
  streak: {
    currentStreak: number;       // e.g. 12
    longestStreak: number;       // e.g. 18
    lastStreakDate: string;      // YYYY-MM-DD
    streakFreezeCount: number;   // e.g. 2
  };
  dailyXp: {
    [dateStr: string]: number;   // "2026-09-03": 85
  };
  stats: {
    totalXp: number;
    lessonsCompletedCount: number;
    totalTimeSpentSeconds: number;
    averageAccuracy: number;
  };
  fluency: {
    score: number;               // 0 - 100
    level: string;               // "Beginner" | "Conversational" | "Improving" | "Advanced" | "Fluent"
    lastCalculatedAt: Timestamp;
  };
}
```

### 4.2 Streak Engine Rules
1. **Daily Calendar Window**: Evaluated based on user timezone (or UTC ISO date).
2. **Consecutive Days**:
   - If user completes $\ge 1$ lesson, conversation, or mission on consecutive calendar days, `currentStreak = currentStreak + 1`.
   - If user already completed an activity today, keep `currentStreak`.
   - If a day is missed:
     - If `streakFreezeCount > 0`, consume 1 freeze and preserve streak.
     - Else, reset `currentStreak = 1`.
3. **Yearly Active Days**:
   - Calculated as the count of distinct `YYYY-MM-DD` dates in `dailyXp` for the current calendar year (e.g. `31/365`).
4. **Current Week Days (`Mo - Su`)**:
   - Identifies the current Monday through Sunday.
   - For each day, checks if `dailyXp[dateStr] > 0` to set `isActive = true`.

### 4.3 Fluency Score Calculation Formula
$$\text{Fluency} = (0.40 \times \text{AvgAccuracy}) + (0.30 \times \text{ConversationQuality}) + (0.20 \times \text{VocabularyMasteredRatio}) + (0.10 \times \text{ConsistencyFactor})$$

- **AvgAccuracy**: 0–100% score across completed lessons.
- **ConversationQuality**: Average pronunciation / turn score from AI conversation sessions.
- **VocabularyMasteredRatio**: Ratio of completed vocabulary items in the current level bracket.
- **ConsistencyFactor**: $\min\left(100, \frac{\text{currentStreak}}{30} \times 100\right)$.

---

## 5. API Specification

### `GET /api/v1/users/analytics`
**Headers**: `Authorization: Bearer <FIREBASE_ID_TOKEN>`  
**Query Params**: `languageCode` (optional, defaults to user's active learning language)

#### Success Response (`200 OK`)
```json
{
  "success": true,
  "data": {
    "currentDate": "2026-09-03",
    "selectedDate": "2026-09-03",
    "streak": {
      "currentStreak": 12,
      "longestStreak": 18,
      "yearlyActiveDays": 31,
      "yearTotalDays": 365,
      "streakFreezeCount": 2,
      "weekDays": [
        { "dayLabel": "Mo", "date": "2026-08-31", "isActive": true, "xp": 45, "isToday": false },
        { "dayLabel": "Tu", "date": "2026-09-01", "isActive": true, "xp": 60, "isToday": false },
        { "dayLabel": "We", "date": "2026-09-02", "isActive": true, "xp": 80, "isToday": false },
        { "dayLabel": "Th", "date": "2026-09-03", "isActive": true, "xp": 50, "isToday": true },
        { "dayLabel": "Fr", "date": "2026-09-04", "isActive": false, "xp": 0, "isToday": false },
        { "dayLabel": "Sa", "date": "2026-09-05", "isActive": false, "xp": 0, "isToday": false },
        { "dayLabel": "Su", "date": "2026-09-06", "isActive": false, "xp": 0, "isToday": false }
      ],
      "heatmap": [
        [0, 1, 2, 2, 1, 0, 0],
        [1, 2, 3, 2, 1, 1, 0],
        [2, 2, 2, 3, 1, 0, 0],
        [1, 1, 2, 2, 2, 1, 0],
        [2, 3, 3, 2, 0, 0, 0]
      ]
    },
    "hero": {
      "totalXp": 1883,
      "lessonsCount": 12,
      "minutesStudied": 32,
      "points": 1248
    },
    "progress": {
      "status": "Improving",
      "weeklyBars": [
        { "day": "Mo", "value": 45, "heightRatio": 0.4 },
        { "day": "Tu", "value": 90, "heightRatio": 0.85 },
        { "day": "We", "value": 30, "heightRatio": 0.3 },
        { "day": "Th", "value": 100, "heightRatio": 0.95 },
        { "day": "Fr", "value": 50, "heightRatio": 0.5 },
        { "day": "Sa", "value": 70, "heightRatio": 0.65 }
      ]
    },
    "fluency": {
      "score": 82,
      "level": "Intermediate",
      "status": "Improving",
      "breakdown": {
        "accuracy": 85,
        "speaking": 80,
        "vocabulary": 78,
        "consistency": 90
      }
    }
  }
}
```

---

## 6. Android Implementation Details

### 6.1 DTOs (`AnalyticsDtos.kt`)
- `AnalyticsResponse`: Root API response payload.
- `StreakInfo`: Contains `currentStreak`, `yearlyActiveDays`, `weekDays`, `heatmap`.
- `WeekDayStatus`: `dayLabel`, `date`, `isActive`, `isToday`.
- `HeroMetrics`: `totalXp`, `lessonsCount`, `minutesStudied`, `points`.
- `ProgressTracker`: `status`, `weeklyBars`.
- `FluencyTracker`: `score`, `level`, `status`, `breakdown`.

### 6.2 ViewModel & State (`AnalyticsViewModel.kt`)
- `AnalyticsUiState`:
  - `Loading`: Displays spinner while fetching analytics.
  - `Success`: Holds current `AnalyticsData` and selected calendar date.
  - `Error`: Displays error message with retry CTA.
- Exposes `selectDate(date: LocalDate)` to inspect activity on selected dates.
- Automatically refreshes on tab activation (`LaunchedEffect(selectedTab)`).

### 6.3 Compose UI Components (`AnalyticsScreen.kt`)
1. `AnalyticsTopBar`: Circular back button and "Analytics" title.
2. `DateSelectorStrip`: Scrollable/centered row with today highlighted in `PeachPill`.
3. `WeeklyStreakCard`: Renders Monday-Sunday days, fire badges inside brown circular outlines, and continuous `PeachPill` bounding active day runs.
4. `HeroExperienceSection`: Bold XP typography with subtitle and 3-column stats.
5. `LearningTrackerSection`: 2-column or staggered grid featuring:
   - `DailyStreakCard`: Using `fire.xml` in white, `31/365`, and 5x7 dot grid.
   - `ProgressCard`: White vertical rounded bars.
   - `FluencyScoreCard`: Circular meter or percentage card.
6. `BottomNavBar`: Integrated with `selectedTab = HomeTab.Chart`.
