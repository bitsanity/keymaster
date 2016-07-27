package a.keymaster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class KeyListAdapter extends BaseAdapter {
    private ArrayList<KeyRow> listData;
    private LayoutInflater layoutInflater;

    public KeyListAdapter(Context aContext, ArrayList<KeyRow> listData ) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem( int position ) {
        return listData.get( position );
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView( int position, View convertView, ViewGroup parent ) {

        ViewHolder holder;
        String keyname;

        if (convertView == null) {
            convertView = layoutInflater.inflate( R.layout.keylist_row, null );

            holder = new ViewHolder();
            holder.keyIconView = (ImageView) convertView.findViewById( R.id.keyicon );
            holder.keyNameView = (TextView) convertView.findViewById( R.id.keyname );

            KeyRow row = listData.get( position );
            keyname = (null != row) ? row.getKeyName() : "";

            int vis = (null != keyname && 0 < keyname.length()) ? View.VISIBLE : View.INVISIBLE;
            holder.keyIconView.setVisibility( vis );
            holder.keyNameView.setVisibility( vis );

            convertView.setTag( holder );
        } else {
            holder = (ViewHolder) convertView.getTag();
            keyname = holder.keyNameView.getText().toString();
        }

        holder.keyNameView.setText( keyname );
        return convertView;
    }

    static class ViewHolder {
        ImageView keyIconView;
        TextView keyNameView;
    }

}
