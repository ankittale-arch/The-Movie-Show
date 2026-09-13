package com.ankitt.themovieshow.feature.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.TmdbImageUrl
import com.ankitt.themovieshow.core.data.model.PersonDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private var personId: Int? = null

    private val _uiState = MutableStateFlow(PersonDetailUiState())
    val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

    /** Idempotent per id, same as [MovieDetailViewModel.load] — a one-shot fetch, not cached in
     * Room, so re-opening the sheet for the same person without leaving the screen is a no-op. */
    fun load(personId: Int) {
        if (this.personId == personId) return
        this.personId = personId
        _uiState.value = PersonDetailUiState(isLoading = true)
        viewModelScope.launch {
            val result = movieRepository.getPersonDetail(personId)
            _uiState.value = result.fold(
                onSuccess = { PersonDetailUiState(person = it.toUi()) },
                onFailure = { PersonDetailUiState(errorMessage = it.message ?: "Couldn't load cast member") },
            )
        }
    }

    private fun PersonDetail.toUi() = PersonDetailUi(
        id = id,
        name = name,
        biography = biography.takeIf { it.isNotBlank() } ?: "No biography available.",
        meta = listOfNotNull(knownForDepartment, placeOfBirth).joinToString(" · ").takeIf { it.isNotBlank() },
        profileUrl = TmdbImageUrl.profile(profilePath),
        knownFor = knownFor.map {
            KnownForMovieUi(
                id = it.id,
                title = it.title,
                posterUrl = TmdbImageUrl.poster(it.posterPath),
                year = it.releaseDate?.takeIf { date -> date.length >= 4 }?.substring(0, 4),
            )
        },
    )
}
