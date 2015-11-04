package dtancompany.gallerytest;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Edward on 2015-08-11.
 */
public class ImagePagerAdapter extends FragmentStatePagerAdapter {

    ArrayList<String> images;
    HashMap<Integer,Fragment> posFragmentMap;

    public ImagePagerAdapter(FragmentManager fm, ArrayList<String> images) {
        super(fm);
        List<Fragment> fragments = fm.getFragments();
        if (fragments != null) {
            FragmentTransaction ft = fm.beginTransaction();
            for (Fragment f : fragments) {
                //You can perform additional check to remove some (not all) fragments:
                if (f instanceof ImagePageFragment) {
                    ft.remove(f);
                }
            }
            ft.commitAllowingStateLoss();

        }
        this.images = images;
        posFragmentMap = new HashMap<>();
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {

        if (posFragmentMap.get(position) != null) {
            return posFragmentMap.get(position);
        } else {
            Fragment frag = ImagePageFragment.newInstance(images.get(position));
            posFragmentMap.put(position, frag);
            return frag;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        posFragmentMap.remove(position);
    }

    @Override
    public int getCount() {
        return images.size();
    }
}

