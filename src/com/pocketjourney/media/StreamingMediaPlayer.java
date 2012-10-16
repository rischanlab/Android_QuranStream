package com.pocketjourney.media;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.example.quranstream.Perulangan;
import com.example.quranstream.R;
import com.example.quranstream.SuratActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * MediaPlayer does not yet support streaming from external URLs so this class
 * provides a pseudo-streaming function by downloading the content incrementally
 * & playing as soon as we get enough audio in our temporary storage.
 */
public class StreamingMediaPlayer {

	private static final int INTIAL_KB_BUFFER = 96 * 10 / 8;// assume
															// 96kbps*10secs/8bits
															// per byte

	private TextView textStreamed;

	private ImageButton playButton;
	private ImageButton repeatButton;
	private ProgressBar progressBar;

	// Track for display by progressBar
	private long mediaLengthInKb, mediaLengthInSeconds;
	private int totalKbRead = 0;
	private int u = 0;
	private int r;
	private int rr;
	// Create Handler to call View updates on the main UI thread.
	private final Handler handler = new Handler();

	private MediaPlayer mediaPlayer;

	private File downloadingMediaFile;

	private boolean isInterrupted;

	private Context context;
	URLConnection cn;
	InputStream stream;
	private int counter = 0;

	private NotificationManager mNotificationManager;
	private int SIMPLE_NOTFICATION_ID;
	Notification notifyDetails;
	Perulangan ulang = new Perulangan();

	public StreamingMediaPlayer(Context context, TextView textStreamed,
			ImageButton playButton, ImageButton repButton, int rep,
			ProgressBar progressBar, NotificationManager nm) {
		this.context = context;
		this.textStreamed = textStreamed;
		this.playButton = playButton;
		this.progressBar = progressBar;
		this.rr = rep;
		this.repeatButton = repButton;
		this.mNotificationManager = nm;
	}

	public void setrepeat(int rt) {
		Log.d("perulangan ==>", String.valueOf(rt));
		this.r = rt;
		ulang.SetPerulangan(rt);
	}

	/**
	 * Progressivly download the media to a temporary location and update the
	 * MediaPlayer as new content becomes available.
	 */
	public void startStreaming(final String mediaUrl, long mediaLengthInKb,
			long mediaLengthInSeconds) {

		notifyDetails = new Notification(R.drawable.ic_play_off,
				"You've got a new notification!", System.currentTimeMillis());
		RemoteViews contentView = new RemoteViews(context.getPackageName(),
				R.layout.notification_custom);
		contentView.setImageViewResource(R.id.notif_icon,
				R.drawable.ic_launcher);
		contentView.setTextViewText(R.id.notif_reciters, "Custom notification");
		contentView.setTextViewText(R.id.notif_surat, "Surat notification");
		// contentView.setTextViewText(R.id.text,
		// "This is a custom layout");
		notifyDetails.contentView = contentView;
		Intent notificationIntent = new Intent(context, SuratActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notifyDetails.contentIntent = contentIntent;

		Log.d("id imgbutton ======>", String.valueOf(playButton.getId()));
		playButton.setEnabled(false);
		progressBar.setVisibility(View.VISIBLE);
		Log.d("status Button=========>", String.valueOf(playButton.isEnabled()));
		this.mediaLengthInKb = mediaLengthInKb;
		this.mediaLengthInSeconds = mediaLengthInSeconds;

		Runnable r = new Runnable() {
			public void run() {
				try {
					downloadAudioIncrement(mediaUrl);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		new Thread(r).start();
	}

	/**
	 * Download the url stream to a temporary location and then call the
	 * setDataSource for that local file
	 * 
	 * @throws IOException
	 */
	public void downloadAudioIncrement(String mediaUrl) throws IOException {
		Log.d("status =========>", "download");
		Log.d("status Button=========>", String.valueOf(playButton.isEnabled()));

		try {
			cn = new URL(mediaUrl).openConnection();
			cn.connect();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			stream = cn.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (stream == null) {
			Log.e(getClass().getName(),
					"Unable to create InputStream for mediaUrl:" + mediaUrl);
		}

		downloadingMediaFile = new File(context.getCacheDir(),
				"downloadingMedia.dat");

		// Just in case a prior deletion failed because our code crashed or
		// something, we also delete any previously
		// downloaded file to ensure we start fresh. If you use this code,
		// always delete
		// no longer used downloads else you'll quickly fill up your hard disk
		// memory. Of course, you can also
		// store any previously downloaded file in a separate data cache for
		// instant replay if you wanted as well.
		if (downloadingMediaFile.exists()) {
			downloadingMediaFile.delete();
		}

		FileOutputStream out = new FileOutputStream(downloadingMediaFile);
		byte buf[] = new byte[16384];
		int totalBytesRead = 0, incrementalBytesRead = 0;
		do {
			int numread = stream.read(buf);
			if (numread <= 0)
				break;
			out.write(buf, 0, numread);
			totalBytesRead += numread;
			incrementalBytesRead += numread;
			totalKbRead = totalBytesRead / 1000;

			testMediaBuffer();
			fireDataLoadUpdate();
		} while (validateNotInterrupted());
		stream.close();
		if (validateNotInterrupted()) {
			fireDataFullyLoaded();
		}
	}

	private boolean validateNotInterrupted() {
		if (isInterrupted) {
			if (mediaPlayer != null) {
				mediaPlayer.pause();
				// mediaPlayer.release();
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Test whether we need to transfer buffered data to the MediaPlayer.
	 * Interacting with MediaPlayer on non-main UI thread can causes crashes to
	 * so perform this using a Handler.
	 */
	private void testMediaBuffer() {
		Runnable updater = new Runnable() {
			public void run() {
				if (mediaPlayer == null) {
					// Only create the MediaPlayer once we have the minimum
					// buffered data
					if (totalKbRead >= INTIAL_KB_BUFFER) {
						try {
							startMediaPlayer();
							Log.d("status =========>", "start");
						} catch (Exception e) {
							Log.e(getClass().getName(),
									"Error copying buffered conent.", e);
						}
					}
				} else if (mediaPlayer.getDuration()
						- mediaPlayer.getCurrentPosition() <= 1000) {
					// NOTE: The media player has stopped at the end so transfer
					// any existing buffered data
					// We test for < 1second of data because the media player
					// can stop when there is still
					// a few milliseconds of data left to play
					transferBufferToMediaPlayer();
				}
			}
		};
		handler.post(updater);
	}

	private void startMediaPlayer() {
		try {
			Log.d("status =========>", "start");
			playButton.setEnabled(true);
			
			Context c = context;
			// CharSequence contentTitle = "Notification Details...";
			// CharSequence contentText =
			// "Browse Android Official Site by clicking me";
			// Intent notifyIntent = new Intent(
			// android.content.Intent.ACTION_VIEW,
			// Uri.parse("http://www.android.com"));
			// PendingIntent intent = PendingIntent
			// .getActivity(context, 0, notifyIntent,
			// android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
			//
			// notifyDetails.setLatestEventInfo(c, contentTitle, contentText,
			// intent);
			// mNotificationManager.notify(SIMPLE_NOTFICATION_ID,
			// notifyDetails);

			progressBar.setVisibility(View.GONE);
			Log.d("status Button=========>",
					String.valueOf(playButton.isEnabled()));
			File bufferedFile = new File(c.getCacheDir(), "playingMedia"
					+ (counter++) + ".dat");

			// We double buffer the data to avoid potential read/write errors
			// that could happen if the
			// download thread attempted to write at the same time the
			// MediaPlayer was trying to read.
			// For example, we can't guarantee that the MediaPlayer won't open a
			// file for playing and leave it locked while
			// the media is playing. This would permanently deadlock the file
			// download. To avoid such a deadloack,
			// we move the currently loaded data to a temporary buffer file that
			// we start playing while the remaining
			// data downloads.
			moveFile(downloadingMediaFile, bufferedFile);

			Log.d(getClass().getName(),
					"Buffered File path: " + bufferedFile.getAbsolutePath());
			Log.d(getClass().getName(),
					"Buffered File length: " + bufferedFile.length() + "");

			mediaPlayer = createMediaPlayer(bufferedFile);

			// We have pre-loaded enough content and started the MediaPlayer so
			// update the buttons & progress meters.

			mediaPlayer.start();
			mNotificationManager.notify(SIMPLE_NOTFICATION_ID, notifyDetails);
			Log.d("loop", "perulangan terjadi " + String.valueOf(r));
			startPlayProgressUpdater();
			// playButton.setEnabled(true);
			Log.d("id imagebutton", String.valueOf(playButton.getId()));

		} catch (IOException e) {
			Log.e(getClass().getName(), "Error initializing the MediaPlayer.",
					e);
			return;
		}
	}

	private MediaPlayer createMediaPlayer(File mediaFile) throws IOException {
		final MediaPlayer mPlayer = new MediaPlayer();
		mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e(getClass().getName(), "Error in MediaPlayer: (" + what
						+ ") with extra (" + extra + ")");
				return false;
			}
		});
		// Log.d("loop", "perulangan terjadi " + String.valueOf(r));
		// String a = String.valueOf(ulang.GetPerulangan());
		// Log.d("perulangan ", a);
		// mediaPlayer.setLooping(true);
		// mediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
		//
		// @Override
		// public void onSeekComplete(MediaPlayer mp) {
		// // TODO Auto-generated method stub
		// u++;
		// Log.d("seek complete", "seek  complete " + String.valueOf(u));
		// Log.d("loop", "perulangan terjadi " + String.valueOf(r));
		// String a = String.valueOf(ulang.GetPerulangan());
		//
		// Log.d("perulangan ", a);
		// if (u == 4) {
		// mediaPlayer.setLooping(false);
		// }
		// }
		// });
		
		mPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				Log.d("complete", "sound complete");
				playButton.setEnabled(true);
				playButton.setBackgroundResource(R.drawable.ic_play_off);
				Log.d("status Button=========>",
						String.valueOf(playButton.isEnabled()));
				mNotificationManager.cancel(SIMPLE_NOTFICATION_ID);
			}
		});
		// It appears that for security/permission reasons, it is better to pass
		// a FileDescriptor rather than a direct path to the File.
		// Also I have seen errors such as "PVMFErrNotSupported" and
		// "Prepare failed.: status=0x1" if a file path String is passed to
		// setDataSource(). So unless otherwise noted, we use a FileDescriptor
		// here.
		FileInputStream fis = new FileInputStream(mediaFile);
		mPlayer.setDataSource(fis.getFD());
		mPlayer.prepare();
		return mPlayer;
	}

	/**
	 * Transfer buffered data to the MediaPlayer. NOTE: Interacting with a
	 * MediaPlayer on a non-main UI thread can cause thread-lock and crashes so
	 * this method should always be called using a Handler.
	 */
	private void transferBufferToMediaPlayer() {
		try {
			// First determine if we need to restart the player after
			// transferring data...e.g. perhaps the user pressed pause
			boolean wasPlaying = mediaPlayer.isPlaying();
			int curPosition = mediaPlayer.getCurrentPosition();

			// Copy the currently downloaded content to a new buffered File.
			// Store the old File for deleting later.
			File oldBufferedFile = new File(context.getCacheDir(),
					"playingMedia" + counter + ".dat");
			File bufferedFile = new File(context.getCacheDir(), "playingMedia"
					+ (counter++) + ".dat");

			// This may be the last buffered File so ask that it be delete on
			// exit. If it's already deleted, then this won't mean anything. If
			// you want to
			// keep and track fully downloaded files for later use, write
			// caching code and please send me a copy.
			bufferedFile.deleteOnExit();
			moveFile(downloadingMediaFile, bufferedFile);

			// Pause the current player now as we are about to create and start
			// a new one. So far (Android v1.5),
			// this always happens so quickly that the user never realized we've
			// stopped the player and started a new one
			mediaPlayer.pause();

			// Create a new MediaPlayer rather than try to re-prepare the prior
			// one.
			mediaPlayer = createMediaPlayer(bufferedFile);
			mediaPlayer.seekTo(curPosition);

			// Restart if at end of prior buffered content or mediaPlayer was
			// previously playing.
			// NOTE: We test for < 1second of data because the media player can
			// stop when there is still
			// a few milliseconds of data left to play
			boolean atEndOfFile = mediaPlayer.getDuration()
					- mediaPlayer.getCurrentPosition() <= 1000;
			if (wasPlaying || atEndOfFile) {
				mediaPlayer.start();
			}

			// Lastly delete the previously playing buffered File as it's no
			// longer needed.
			oldBufferedFile.delete();

		} catch (Exception e) {
			Log.e(getClass().getName(),
					"Error updating to newly loaded content.", e);
		}
	}

	private void fireDataLoadUpdate() {
		Runnable updater = new Runnable() {
			public void run() {
				// textStreamed.setText((totalKbRead + " Kb read"));
				float loadProgress = ((float) totalKbRead / (float) mediaLengthInKb);
				// progressBar.setSecondaryProgress((int)(loadProgress*100));
			}
		};
		handler.post(updater);
	}

	private void fireDataFullyLoaded() {
		Runnable updater = new Runnable() {
			public void run() {
				transferBufferToMediaPlayer();

				// Delete the downloaded File as it's now been transferred to
				// the currently playing buffer file.
				downloadingMediaFile.delete();
				// textStreamed.setText(("Audio full loaded: " + totalKbRead +
				// " Kb read"));
			}
		};
		handler.post(updater);
	}

	public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}

	public void startPlayProgressUpdater() {
		// float progress = (((float) mediaPlayer.getCurrentPosition() / 1000) /
		// mediaLengthInSeconds);
		// progressBar.setProgress((int) (progress * 100));

		if (mediaPlayer.isPlaying()) {
			// Log.d("loop", "abc");
			Runnable notification = new Runnable() {
				public void run() {
					startPlayProgressUpdater();
				}
			};
			handler.postDelayed(notification, 1000);
		} else {
			Log.d("loop complete", "complete abc");
		}
	}

	public void interrupt() {
		playButton.setEnabled(false);
		isInterrupted = true;
		validateNotInterrupted();
	}

	/**
	 * Move the file in oldLocation to newLocation.
	 */
	public void moveFile(File oldLocation, File newLocation) throws IOException {

		if (oldLocation.exists()) {
			BufferedInputStream reader = new BufferedInputStream(
					new FileInputStream(oldLocation));
			BufferedOutputStream writer = new BufferedOutputStream(
					new FileOutputStream(newLocation, false));
			try {
				byte[] buff = new byte[8192];
				int numChars;
				while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
					writer.write(buff, 0, numChars);
				}
			} catch (IOException ex) {
				throw new IOException("IOException when transferring "
						+ oldLocation.getPath() + " to "
						+ newLocation.getPath());
			} finally {
				try {
					if (reader != null) {
						writer.close();
						reader.close();
					}
				} catch (IOException ex) {
					Log.e(getClass().getName(),
							"Error closing files when transferring "
									+ oldLocation.getPath() + " to "
									+ newLocation.getPath());
				}
			}
		} else {
			throw new IOException(
					"Old location does not exist when transferring "
							+ oldLocation.getPath() + " to "
							+ newLocation.getPath());
		}
	}
}
