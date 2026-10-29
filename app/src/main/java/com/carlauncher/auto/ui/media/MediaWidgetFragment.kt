package com.carlauncher.auto.ui.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.carlauncher.auto.R
import com.carlauncher.auto.databinding.FragmentMediaWidgetBinding
import com.carlauncher.auto.model.MediaTrack
import com.carlauncher.auto.service.MediaControllerManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MediaWidgetFragment : Fragment() {

    private var _binding: FragmentMediaWidgetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaWidgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        observePlayback()
    }

    private fun setupButtons() {
        binding.btnMediaPlayPause.setOnClickListener {
            MediaControllerManager.playPause()
        }

        binding.btnMediaNext.setOnClickListener {
            MediaControllerManager.skipNext()
        }

        binding.btnMediaPrev.setOnClickListener {
            MediaControllerManager.skipPrevious()
        }

        binding.btnOpenSpotifyFallback.setOnClickListener {
            MediaControllerManager.openSpotify(requireContext())
        }

        // Tapping the album art or media info opens Spotify
        binding.cardAlbumArt.setOnClickListener {
            MediaControllerManager.openSpotify(requireContext())
        }
    }

    private fun observePlayback() {
        viewLifecycleOwner.lifecycleScope.launch {
            MediaControllerManager.currentTrack.collectLatest { track ->
                updateUi(track)
            }
        }
    }

    private fun updateUi(track: MediaTrack?) {
        if (track == null || (track.title.isNullOrBlank() && !track.isPlaying)) {
            // Show empty state
            binding.layoutMediaActive.visibility = View.GONE
            binding.layoutMediaEmpty.visibility = View.VISIBLE
            return
        }

        binding.layoutMediaActive.visibility = View.VISIBLE
        binding.layoutMediaEmpty.visibility = View.GONE

        // Update Track Title & Artist
        binding.tvTrackTitle.text = track.title ?: getString(R.string.unknown_title)
        binding.tvArtistName.text = track.artist ?: getString(R.string.unknown_artist)

        // Source badge (Spotify / Other)
        if (track.packageName == MediaControllerManager.SPOTIFY_PACKAGE) {
            binding.ivMediaSourceIcon.setImageResource(R.drawable.ic_spotify)
            binding.tvMediaSourceName.text = getString(R.string.spotify_source)
            binding.tvMediaSourceName.setTextColor(ContextCompat.getColor(requireContext(), R.color.spotify_green))
        } else {
            binding.ivMediaSourceIcon.setImageResource(R.drawable.ic_music)
            binding.tvMediaSourceName.text = track.appName ?: getString(R.string.media_source_unknown)
            binding.tvMediaSourceName.setTextColor(ContextCompat.getColor(requireContext(), R.color.car_text_secondary))
        }

        // Album Art
        if (track.albumArt != null) {
            binding.ivAlbumArt.clearColorFilter()
            binding.ivAlbumArt.setImageBitmap(track.albumArt)
        } else {
            binding.ivAlbumArt.setImageResource(R.drawable.ic_music)
            binding.ivAlbumArt.setColorFilter(ContextCompat.getColor(requireContext(), R.color.car_text_dim))
        }

        // Play / Pause Icon
        val playPauseIcon = if (track.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        binding.ivPlayPauseIcon.setImageResource(playPauseIcon)

        // Progress Bar
        if (track.duration > 0) {
            val progress = ((track.position.toDouble() / track.duration.toDouble()) * 100).toInt()
            binding.pbTrackProgress.progress = progress.coerceIn(0, 100)
        } else {
            binding.pbTrackProgress.progress = 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
