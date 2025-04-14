# Artha-Sakha (Budget Buddy)

Artha-Sakha is an Android application designed to help users track, analyze, and optimize their spending. It integrates with UPI payment gateways such as Paytm, PhonePe, and Google Pay, allowing users to easily manage their expenses and create smart budgets.

## Features

- **Expense Tracking**: Users can add and track their daily expenses with categories, hashtags, and amounts.
- **Spending Analytics**: Visualize your expenses through charts to understand your spending habits.
- **Smart Budgeting**: Set budget limits and receive notifications when you exceed them.
- **UPI Integration**: Make payments through UPI with a QR code scanner that redirects to apps like Paytm, PhonePe, and Google Pay.
- **User Authentication**: Firebase authentication is used for secure user login and registration.
- **Data Storage**: All expense data is stored locally using SharedPreferences, with plans for future migration to SQLite.
- **Admin View**: Admins can manage user data and see overall statistics.

## Tech Stack

- **Frontend**: Android Studio (Kotlin)
- **Backend**: Firebase (for authentication)
- **Database**: SharedPreferences (local storage), with plans to use SQLite in the future.
- **UPI Integration**: Paytm, PhonePe, Google Pay (via QR code scanning)
- **Charting**: BarChart for displaying recent expenses.

## Getting Started

### Prerequisites

- Android Studio 4.x or later
- A physical or virtual Android device for testing

### Clone the repository

To get started, clone the repository:

```bash
git clone https://github.com/Raghvendra-Chauhan/Artha-Sakha-App.git
