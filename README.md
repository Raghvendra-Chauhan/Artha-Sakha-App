# Artha-Sakha-App
<br>
# Artha-Sakha (Budget Buddy)
<br>

Artha-Sakha is an Android application designed to help users track, analyze, and optimize their spending. It integrates with UPI payment gateways such as Paytm, PhonePe, and Google Pay, allowing users to easily manage their expenses and create smart budgets.
<br>

## Features
<br>

- **Expense Tracking**: Users can add and track their daily expenses with categories, hashtags, and amounts.
- <br>
- **Spending Analytics**: Visualize your expenses through charts to understand your spending habits.
- <br>
- **Smart Budgeting**: Set budget limits and receive notifications when you exceed them.
- <br>
- **UPI Integration**: Make payments through UPI with a QR code scanner that redirects to apps like Paytm, PhonePe, and Google Pay.
- <br>
- **User Authentication**: Firebase authentication is used for secure user login and registration.
- <br>
- **Data Storage**: All expense data is stored locally using SharedPreferences, with plans for future migration to SQLite.
- <br>
- **Admin View**: Admins can manage user data and see overall statistics.
- <br>

## Tech Stack
<br>

- **Frontend**: Android Studio (Kotlin)
- <br>
- **Backend**: Firebase (for authentication)
- <br>
- **Database**: SharedPreferences (local storage), with plans to use SQLite in the future.
- <br>
- **UPI Integration**: Paytm, PhonePe, Google Pay (via QR code scanning)
- <br>
- **Charting**: BarChart for displaying recent expenses.
- <br>

## Getting Started
<br>

### Prerequisites
<br>

- Android Studio 4.x or later
- <br>
- A physical or virtual Android device for testing
- <br>

### Clone the repository
<br>

To get started, clone the repository:
<br>

```bash
<br>
git clone https://github.com/Raghvendra-Chauhan/Artha-Sakha-App.git
<br>
```
<br>

### Install dependencies
<br>

Open the project in Android Studio and wait for the dependencies to be installed automatically.
<br>
### Running the App
<br>

1. Open `Artha-Sakha` project in Android Studio.
2. Connect an Android device or start an emulator.
3. Run the project by clicking the **Run** button in Android Studio.
   <br>

### Usage
<br>

1. **Greeting Page**: On first launch, users will see a greeting page.
2. **Login/Registration**: Users can either log in or skip directly to the main page.
3. **Main Page**: The main page allows users to add expenses, view charts, and access payment options.
4. **Admin Panel**: Admins can view and manage app settings.
   <br>

## Contributing
<br>

Feel free to fork the project and submit pull requests for improvements, features, or bug fixes. Please ensure any new code includes tests, and update the README if necessary.
<br>

## License
<br>

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
