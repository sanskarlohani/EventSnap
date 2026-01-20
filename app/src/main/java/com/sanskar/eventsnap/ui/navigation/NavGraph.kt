package com.sanskar.eventsnap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import com.sanskar.eventsnap.data.model.Event
import com.sanskar.eventsnap.data.model.EventSource
import com.sanskar.eventsnap.ui.screens.AddEventScreen
import com.sanskar.eventsnap.ui.screens.AuthScreen
import com.sanskar.eventsnap.ui.screens.EventDetailScreen
import com.sanskar.eventsnap.ui.screens.HomeScreen
import com.sanskar.eventsnap.ui.screens.SignUpScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    val isSignedIn = FirebaseAuth.getInstance().currentUser != null

    NavHost(
        navController = navController,
        startDestination = if (isSignedIn) Screen.Home else Screen.Auth
    ) {
        composable<Screen.Auth> {
            AuthScreen(
                onSignedIn = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Auth) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp)
                }
            )
        }

        composable<Screen.SignUp> {
            SignUpScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignedIn = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Auth) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Home> {
            HomeScreen(
                onNavigateToAddEvent = {
                    navController.navigate(Screen.AddEvent)
                },
                onNavigateToEventDetail = { event ->
                    navController.navigate(
                        Screen.EventDetail(
                            eventId = event.id,
                            source = event.source.name,
                            title = event.title,
                            date = event.date,
                            description = event.description,
                            notes = event.notes
                        )
                    )
                },
                onLogout = {
                    navController.navigate(Screen.Auth) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.AddEvent> {
            AddEventScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEventAdded = {
                    // Be explicit: go to Home and avoid leaving AddEvent on the back stack.
                    navController.navigate(Screen.Home) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(Screen.Home) { inclusive = false }
                    }
                }
            )
        }

        composable<Screen.EventDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.EventDetail>()
            val event = Event(
                id = args.eventId,
                title = args.title,
                date = args.date,
                description = args.description,
                notes = args.notes,
                source = EventSource.valueOf(args.source)
            )

            EventDetailScreen(
                passedEvent = event,
                onNavigateBack = {
                    // Always return to Home after leaving details.
                    navController.navigate(Screen.Home) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(Screen.Home) { inclusive = false }
                    }
                }
            )
        }
    }
}
