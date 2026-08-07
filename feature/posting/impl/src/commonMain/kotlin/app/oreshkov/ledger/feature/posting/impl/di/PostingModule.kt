package app.oreshkov.ledger.feature.posting.impl.di

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import app.oreshkov.ledger.core.data.di.DataModule
import app.oreshkov.ledger.core.domain.di.DomainModule
import app.oreshkov.ledger.core.navigation.LocalNavigator
import app.oreshkov.ledger.core.navigation.TopLevelDestination
import app.oreshkov.ledger.feature.posting.api.navigation.PostingDetail
import app.oreshkov.ledger.feature.posting.api.navigation.PostingEdit
import app.oreshkov.ledger.feature.posting.api.navigation.PostingList
import app.oreshkov.ledger.feature.posting.impl.PostingDetailsScreen
import app.oreshkov.ledger.feature.posting.impl.PostingEditScreen
import app.oreshkov.ledger.feature.posting.impl.PostingListScreen
import app.oreshkov.ledger.feature.posting.impl.resources.Res
import app.oreshkov.ledger.feature.posting.impl.resources.posting_nav_label
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@Module(includes = [DataModule::class, DomainModule::class])
@ComponentScan("app.oreshkov.ledger.feature.posting.impl")
class PostingModule

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3AdaptiveApi::class)
val postingNavigationModule = module {
    // Contributes this feature as a top-level destination. A distinct qualifier keeps
    // it from overriding other features' destinations; the app aggregates via getAll().
    single(named("posting_top_level")) {
        TopLevelDestination(
            key = PostingList,
            label = Res.string.posting_nav_label,
            icon = Icons.AutoMirrored.Filled.List,
            order = 0,
        )
    }

    navigation<PostingList>(
        metadata = ListDetailSceneStrategy.listPane()
    ) {
        val navigator = LocalNavigator.current
        PostingListScreen(
            onNavigateToEdit = { id -> navigator.goTo(PostingEdit(id)) },
            onNavigateToDetails = { id -> navigator.goTo(PostingDetail(id)) },
            viewModel = koinViewModel()
        )
    }

    navigation<PostingDetail>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { route ->
        val navigator = LocalNavigator.current
        // ViewModel identity is scoped to this back-stack entry by
        // rememberViewModelStoreNavEntryDecorator (see App.kt); no manual key needed.
        PostingDetailsScreen(
            onNavigateBack = { navigator.goBack() },
            onEditClick = { id -> navigator.goTo(PostingEdit(id)) },
            onDeleted = { navigator.goBack() },
            viewModel = koinViewModel(
                parameters = { parametersOf(route.id) }
            )
        )
    }

    navigation<PostingEdit>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { route ->
        val navigator = LocalNavigator.current
        PostingEditScreen(
            onNavigateBack = { navigator.goBack() },
            viewModel = koinViewModel(
                parameters = { parametersOf(route.id) }
            )
        )
    }
}