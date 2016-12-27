package tk.leoforney.spotifydownloader;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

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
    }

    @Override
    public int getItemCount() {
        return (null != downloads ? downloads.size() : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView playlistTextView;
        CircularProgressView cpv;

        public ViewHolder(View itemView) {
            super(itemView);
            this.playlistTextView = (TextView) itemView.findViewById(R.id.playlistTextView);
            this.cpv = (CircularProgressView) itemView.findViewById(R.id.progressView);
        }
    }
}
