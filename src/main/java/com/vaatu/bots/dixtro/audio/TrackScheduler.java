package com.vaatu.bots.dixtro.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.vaatu.bots.dixtro.embed.MusicEmbedFactory;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Optional;

@RequiredArgsConstructor
public class TrackScheduler extends AudioEventAdapter {
    private final GuildTrackManager trackManager;

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        MessageEmbed embed = MusicEmbedFactory.createSongEmbed(track.getInfo());
        trackManager.announceInChannel(embed);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        Optional<AudioTrack> nextTrack = Optional.ofNullable(trackManager.getQueue().poll());
        if (nextTrack.isPresent() && (endReason.mayStartNext)) {
            player.startTrack(nextTrack.get(), false);
        } else if (nextTrack.isEmpty() & trackManager.trackIsEmpty()) {
            MessageEmbed embed = MusicEmbedFactory.createFinishedTracks();
            trackManager.announceInChannel(embed);
            trackManager.disconnectVoiceManager();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        boolean lastSong = trackManager.getQueue().isEmpty();
        if (exception.severity.equals(FriendlyException.Severity.SUSPICIOUS) && lastSong) {
            trackManager.disconnectVoiceManager();
        }
    }
}
