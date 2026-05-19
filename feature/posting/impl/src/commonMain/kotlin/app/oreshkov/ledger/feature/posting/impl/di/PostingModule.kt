package app.oreshkov.ledger.feature.posting.impl.di

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.saveable.rememberSaveable
import app.oreshkov.ledger.core.data.di.DataModule
import app.oreshkov.ledger.core.navigation.Navigator
import app.oreshkov.ledger.feature.posting.api.navigation.PostingDetail
import app.oreshkov.ledger.feature.posting.api.navigation.PostingEdit
import app.oreshkov.ledger.feature.posting.api.navigation.PostingList
import app.oreshkov.ledger.feature.posting.impl.PostingDetailsScreen
import app.oreshkov.ledger.feature.posting.impl.PostingEditScreen
import app.oreshkov.ledger.feature.posting.impl.PostingListScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.Module
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Module(includes = [DataModule::class])
@ComponentScan("app.oreshkov.ledger.feature.posting.impl")
class PostingModule

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalUuidApi::class
)
val postingNavigationModule = module {
    navigation<PostingList>(
        metadata = ListDetailSceneStrategy.listPane()
    ) {
        val navigator = koinInject<Navigator>()
        PostingListScreen(
            onNavigateToEdit = { id -> navigator.goTo(PostingEdit(id)) },
            onNavigateToDetails = { id -> navigator.goTo(PostingDetail(id)) },
            viewModel = koinViewModel()
        )
    }

    navigation<PostingDetail>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { route ->
        val navigator = koinInject<Navigator>()
        val vmKey = rememberSaveable(route) { "posting_details_${route.id}_${Uuid.random()}" }
        PostingDetailsScreen(
            onNavigateBack = { navigator.goBack() },
            onEditClick = { id -> navigator.goTo(PostingEdit(id)) },
            onDeleted = { navigator.goBack() },
            viewModel = koinViewModel(
                key = vmKey,
                parameters = { parametersOf(route.id) }
            )
        )
    }

    navigation<PostingEdit>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { route ->
        val navigator = koinInject<Navigator>()
        val vmKey = rememberSaveable(route) { "posting_edit_${route.id ?: "new"}_${Uuid.random()}" }

        PostingEditScreen(
            onNavigateBack = { navigator.goBack() },
            viewModel = koinViewModel(
                key = vmKey,
                parameters = { parametersOf(route.id) }
            )
        )
    }
}