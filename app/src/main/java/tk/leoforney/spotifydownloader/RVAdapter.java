package tk.leoforney.spotifydownloader;

import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dynamic Signals on 11/28/2016.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {

    List<PlaylistDownload> downloads = new ArrayList<>();

    public int setDownload(int index, PlaylistDownload download) {
        downloads.add(index, download);
        return downloads.indexOf(download);
    }

    public int addDownload(PlaylistDownload download) {
        downloads.add(download);
        return downloads.indexOf(download);
    }

    @Override
    public RVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_downloader_recyclerview_item, null);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RVAdapter.ViewHolder holder, int position) {

        holder.playlistTextView.setText(downloads.get(position).playlist.name);
        holder.cpv.setProgress(downloads.get(position).status);
        if (downloads.get(position).status >= 100) {
            holder.cpv.setVisibility(View.INVISIBLE);
            holder.refreshButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setVisibility(View.VISIBLE);
        }

        holder.cancelButton.setOnClickListener(v -> {
            removeAt(position);
            holder.onClick(holder.cancelButton);
        });
        holder.refreshButton.setOnClickListener(holder);

    }

    @Override
    public int getItemCount() {
        return (null != downloads ? downloads.size() : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView playlistTextView;
        public CircularProgressView cpv;
        public ImageButton cancelButton, refreshButton;

        public ViewHolder(View itemView) {
            super(itemView);
            this.playlistTextView = (TextView) itemView.findViewById(R.id.playlistTextView);
            this.cpv = (CircularProgressView) itemView.findViewById(R.id.progressView);
            this.cancelButton = (ImageButton) itemView.findViewById(R.id.deleteButtonPlaylist);
            this.refreshButton = (ImageButton) itemView.findViewById(R.id.refreshButtonPlaylist);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.deleteButtonPlaylist:
                    Log.d(getClass().getName(), "delete button clicked on " + playlistTextView.getText().toString());
                    File playlistDirectory = new File(Environment.getExternalStorageDirectory() + "/Music/" + playlistTextView.getText().toString());
                    boolean successfullyDeleted = RVAdapter.deleteDirectory(playlistDirectory);
                    Log.d(getClass().getSimpleName(), playlistDirectory.getPath() + " has been deleted " + (successfullyDeleted ? "correctly" : "incorrectly"));

                    break;
                case R.id.refreshButtonPlaylist:
                    Log.d(getClass().getName(), "refresh button clicked on " + playlistTextView.getText().toString());
                    break;
            }
        }
    }

    static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public void removeAt(int position) {
        downloads.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, downloads.size());
    }
}
