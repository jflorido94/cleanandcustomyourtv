package com.gathin.tvhome;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;

import java.util.Locale;

/**
 * The Live TV tile's intent: the same tuned channel the Inputs menu's "TV en vivo" row opens, so the tile tunes the
 * TV instead of resuming the last input. Null when no tuner is found or the Live TV app does not handle the URI;
 * the caller then opens the app as usual.
 */
final class LiveTvTune {

    private LiveTvTune() {
    }

    static Intent tunedIntent(Context ctx, String liveTvPkg, String liveTvInputId) {
        TvInputManager tim = (TvInputManager) ctx.getSystemService(Context.TV_INPUT_SERVICE);
        if (tim == null || liveTvPkg == null || liveTvPkg.isEmpty()) return null;
        String tunerId = null;
        boolean tunerIsHw = false;
        boolean tunerIsAnalog = false;
        for (TvInputInfo info : tim.getTvInputList()) {
            if (info.getType() != TvInputInfo.TYPE_TUNER) continue;
            if ((info.getServiceInfo().applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) continue;
            if (!liveTvInputId.isEmpty() && !liveTvInputId.equals(info.getId())) continue;
            // Same choice as the Inputs menu: a hardware tuner first, and not an analog one when another is there.
            boolean hw = info.getId().matches(".*/HW\\d+$");
            boolean analog = info.getId().toLowerCase(Locale.ROOT).contains("analog");
            boolean better = tunerId == null
                    || (hw && !tunerIsHw)
                    || (hw == tunerIsHw && tunerIsAnalog && !analog)
                    || (hw == tunerIsHw && analog == tunerIsAnalog && info.getId().compareTo(tunerId) < 0);
            if (better) {
                tunerId = info.getId();
                tunerIsHw = hw;
                tunerIsAnalog = analog;
            }
        }
        if (tunerId == null) return null;
        boolean configured = !liveTvInputId.isEmpty();
        Intent tuned = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(configured ? TvContract.buildChannelsUriForInput(tunerId)
                                : TvContract.buildChannelUriForPassthroughInput(tunerId),
                        configured ? TvContract.Channels.CONTENT_TYPE : TvContract.Channels.CONTENT_ITEM_TYPE)
                .setPackage(liveTvPkg);
        return tuned.resolveActivity(ctx.getPackageManager()) != null ? tuned : null;
    }
}
