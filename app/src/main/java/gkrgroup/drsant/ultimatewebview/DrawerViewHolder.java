package gkrgroup.drsant.ultimatewebview;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import gkrgroup.drsant.ultimatewebview.Fragments.AboutUs;
import gkrgroup.drsant.ultimatewebview.Fragments.TestUngFragment;
import gkrgroup.drsant.ultimatewebview.Fragments.WebViewFragment;

/**
 * Created by AbhiAndroid
 */

public class DrawerViewHolder extends RecyclerView.ViewHolder {
    TextView title;
    ImageView icon;

    public DrawerViewHolder(final Context context, View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.title);
        icon = (ImageView) itemView.findViewById(R.id.titleIcon);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDrawerAdapter.selected_item = getPosition();
                MainActivity.title.setText(MainActivity.menuTitles.get(getPosition()));
                Bundle bundle = new Bundle();
                WebViewFragment webViewFragment = new WebViewFragment();
                switch (getPosition()) {

                    case 0:
                        bundle.putString("url", Config.homeUrl);
                        webViewFragment.setArguments(bundle);
                        ((MainActivity) context).loadFragment(webViewFragment, false,"webViewFragment");
                        break;
                    case 1:
                        bundle.putString("url",Config.storeUrl );
                        webViewFragment.setArguments(bundle);
                        ((MainActivity) context).loadFragment(webViewFragment, false,"webViewFragment");
                        break;
                    case 2:
                        bundle.putString("url", Config.serviceUrl);
                        webViewFragment.setArguments(bundle);
                        ((MainActivity) context).loadFragment(webViewFragment, false,"webViewFragment");
                        break;
                    case 3:
                        bundle.putString("url", Config.supportUrl);
                        webViewFragment.setArguments(bundle);
                        ((MainActivity) context).loadFragment(webViewFragment, false,"webViewFragment");
                        break;

                    case 4:
                        bundle.putString("url", Config.contactUrl);
                        webViewFragment.setArguments(bundle);
                        ((MainActivity) context).loadFragment(webViewFragment, false,"webViewFragment");
                        break;

                    case 5:
                        ((MainActivity) context).loadFragment(new AboutUs(), false);
                        break;
                    case 6:
                        Log.d("11MayV1", "You Click Test myMaster");
                        ((MainActivity)context).loadFragment(new TestUngFragment(), false);
                        break;


                }
                MainActivity.customDrawerAdapter.notifyDataSetChanged();
                MainActivity.drawerLayout.closeDrawers();

            }
        });
    }
}
