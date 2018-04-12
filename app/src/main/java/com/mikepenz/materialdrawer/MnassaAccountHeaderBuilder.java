package com.mikepenz.materialdrawer;

import android.os.Build;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.holder.ColorHolder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.icons.MaterialDrawerFont;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.mikepenz.materialdrawer.view.BezelImageView;
import com.mikepenz.materialize.util.UIUtils;
import com.mnassa.extensions.ImageViewExtensionsKt;

/**
 * Created by mikepenz on 23.05.15.
 */
public class MnassaAccountHeaderBuilder extends AccountHeaderBuilder {
    // global references to views we need later
    protected ImageView mCurrentProfileView;

    /**
     * method to build the header view
     *
     * @return
     */
    @Override
    public AccountHeader build() {
        // if the user has not set a accountHeader use the default one :D
        if (mAccountHeaderContainer == null) {
            withAccountHeader(-1);
        }

        // get the header view within the container
        mAccountHeader = mAccountHeaderContainer.findViewById(R.id.material_drawer_account_header);

        //the default min header height by default 148dp
        int defaultHeaderMinHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.material_drawer_account_header_height);
        int statusBarHeight = UIUtils.getStatusBarHeight(mActivity, true);

        // handle the height for the header
        int height;
        if (mHeight != null) {
            height = mHeight.asPixel(mActivity);
        } else {
            if (mCompactStyle) {
                height = mActivity.getResources().getDimensionPixelSize(R.dimen.material_drawer_account_header_height_compact);
            } else {
                //calculate the header height by getting the optimal drawer width and calculating it * 9 / 16
                height = (int) (DrawerUIUtils.getOptimalDrawerWidth(mActivity) * AccountHeader.NAVIGATION_DRAWER_ACCOUNT_ASPECT_RATIO);

                //if we are lower than api 19 (>= 19 we have a translucentStatusBar) the height should be a bit lower
                //probably even if we are non translucent on > 19 devices?
                if (Build.VERSION.SDK_INT < 19) {
                    int tempHeight = height - statusBarHeight;
                    //if we are lower than api 19 we are not able to have a translucent statusBar so we remove the height of the statusBar from the padding
                    //to prevent display issues we only reduce the height if we still fit the required minHeight of 148dp (R.dimen.material_drawer_account_header_height)
                    //we remove additional 8dp from the defaultMinHeaderHeight as there is some buffer in the header and to prevent to large spacings
                    if (tempHeight > defaultHeaderMinHeight - UIUtils.convertDpToPixel(8, mActivity)) {
                        height = tempHeight;
                    }
                }
            }
        }

        // handle everything if we have a translucent status bar which only is possible on API >= 19
        if (mTranslucentStatusBar && Build.VERSION.SDK_INT >= 21) {
            mAccountHeader.setPadding(mAccountHeader.getPaddingLeft(), mAccountHeader.getPaddingTop() + statusBarHeight, mAccountHeader.getPaddingRight(), mAccountHeader.getPaddingBottom());
            //in fact it makes no difference if we have a translucent statusBar or not. we want 9/16 just if we are not compact
            if (mCompactStyle) {
                height = height + statusBarHeight;
            } else if ((height - statusBarHeight) <= defaultHeaderMinHeight) {
                //if the height + statusBar of the header is lower than the required 148dp + statusBar we change the height to be able to display all the data
                height = defaultHeaderMinHeight + statusBarHeight;
            }
        }

        //set the height for the header
        setHeaderHeight(height);

        // get the background view
        mAccountHeaderBackground = (ImageView) mAccountHeaderContainer.findViewById(R.id.material_drawer_account_header_background);
        // set the background
        ImageHolder.applyTo(mHeaderBackground, mAccountHeaderBackground, DrawerImageLoader.Tags.ACCOUNT_HEADER.name());

        if (mHeaderBackgroundScaleType != null) {
            mAccountHeaderBackground.setScaleType(mHeaderBackgroundScaleType);
        }

        // get the text color to use for the text section
        int textColor = ColorHolder.color(mTextColor, mActivity, R.attr.material_drawer_header_selection_text, R.color.material_drawer_header_selection_text);

        // set the background for the section
        if (mCompactStyle) {
            mAccountHeaderTextSection = mAccountHeader;
        } else {
            mAccountHeaderTextSection = mAccountHeaderContainer.findViewById(R.id.material_drawer_account_header_text_section);
        }

        mAccountHeaderTextSectionBackgroundResource = UIUtils.getSelectableBackgroundRes(mActivity);
        handleSelectionView(mCurrentProfile, true);

        // set the arrow :D
        mAccountSwitcherArrow = (ImageView) mAccountHeaderContainer.findViewById(R.id.material_drawer_account_header_text_switcher);
        mAccountSwitcherArrow.setImageDrawable(new IconicsDrawable(mActivity, MaterialDrawerFont.Icon.mdf_arrow_drop_down).sizeRes(R.dimen.material_drawer_account_header_dropdown).paddingRes(R.dimen.material_drawer_account_header_dropdown_padding).color(textColor));

        //get the fields for the name
        mCurrentProfileView = mAccountHeaderContainer.findViewById(R.id.material_drawer_account_header_current);
        mCurrentProfileName = (TextView) mAccountHeader.findViewById(R.id.material_drawer_account_header_name);
        mCurrentProfileEmail = (TextView) mAccountHeader.findViewById(R.id.material_drawer_account_header_email);

        //set the typeface for the AccountHeader
        if (mNameTypeface != null) {
            mCurrentProfileName.setTypeface(mNameTypeface);
        } else if (mTypeface != null) {
            mCurrentProfileName.setTypeface(mTypeface);
        }

//        if (mEmailTypeface != null) {
//            mCurrentProfileEmail.setTypeface(mEmailTypeface);
//        } else if (mTypeface != null) {
//            mCurrentProfileEmail.setTypeface(mTypeface);
//        }
//
//        mCurrentProfileName.setTextColor(textColor);
//        mCurrentProfileEmail.setTextColor(textColor);

        mProfileFirstView = (BezelImageView) mAccountHeader.findViewById(R.id.material_drawer_account_header_small_first);
        mProfileSecondView = (BezelImageView) mAccountHeader.findViewById(R.id.material_drawer_account_header_small_second);
        mProfileThirdView = (BezelImageView) mAccountHeader.findViewById(R.id.material_drawer_account_header_small_third);

        //calculate the profiles to set
        calculateProfiles();

        //process and build the profiles
        buildProfiles();

        // try to restore all saved values again
        if (mSavedInstance != null) {
            int selection = mSavedInstance.getInt(AccountHeader.BUNDLE_SELECTION_HEADER, -1);
            if (selection != -1) {
                //predefine selection (should be the first element
                if (mProfiles != null && (selection) > -1 && selection < mProfiles.size()) {
                    switchProfiles(mProfiles.get(selection));
                }
            }
        }

        //everything created. now set the header
        if (mDrawer != null) {
            mDrawer.setHeader(mAccountHeaderContainer, mPaddingBelowHeader, mDividerBelowHeader);
        }

        //forget the reference to the activity
        mActivity = null;

        return new AccountHeader(this);
    }

    /**
     * helper method to build the views for the ui
     */
    protected void buildProfiles() {
        mCurrentProfileView.setVisibility(View.INVISIBLE);
        mAccountHeaderTextSection.setVisibility(View.INVISIBLE);
        mAccountSwitcherArrow.setVisibility(View.GONE);
        mProfileFirstView.setVisibility(View.GONE);
        mProfileFirstView.setOnClickListener(null);
        mProfileSecondView.setVisibility(View.GONE);
        mProfileSecondView.setOnClickListener(null);
        mProfileThirdView.setVisibility(View.GONE);
        mProfileThirdView.setOnClickListener(null);
        mCurrentProfileName.setText("");
        mCurrentProfileEmail.setText("");

        //we only handle the padding if we are not in compact mode
        if (!mCompactStyle) {
            mAccountHeaderTextSection.setPadding(0, 0, (int) UIUtils.convertDpToPixel(56, mAccountHeaderTextSection.getContext()), 0);
        }

        handleSelectionView(mCurrentProfile, true);

        if (mCurrentProfile != null) {
            if ((mProfileImagesVisible || mOnlyMainProfileImageVisible) && !mOnlySmallProfileImagesVisible) {
                ImageViewExtensionsKt.avatarSquare(mCurrentProfileView, mCurrentProfile.getIcon().getUri().toString());
//                setImageOrPlaceholder(mCurrentProfileView, mCurrentProfile.getIcon());
//                if (mProfileImagesClickable) {
//                    mCurrentProfileView.setOnClickListener(onCurrentProfileClickListener);
//                    mCurrentProfileView.setOnLongClickListener(onCurrentProfileLongClickListener);
//                    mCurrentProfileView.disableTouchFeedback(false);
//                } else {
//                    mCurrentProfileView.disableTouchFeedback(true);
//                }
                mCurrentProfileView.setVisibility(View.VISIBLE);

                mCurrentProfileView.invalidate();
            } else if (mCompactStyle) {
                mCurrentProfileView.setVisibility(View.GONE);
            }

            mAccountHeaderTextSection.setVisibility(View.VISIBLE);
            handleSelectionView(mCurrentProfile, true);
            mAccountSwitcherArrow.setVisibility(View.VISIBLE);
            mCurrentProfileView.setTag(R.id.material_drawer_profile_header, mCurrentProfile);

            StringHolder.applyTo(mCurrentProfile.getName(), mCurrentProfileName);
            StringHolder.applyTo(mCurrentProfile.getEmail(), mCurrentProfileEmail);

            if (mProfileFirst != null && mProfileImagesVisible && !mOnlyMainProfileImageVisible) {
                setImageOrPlaceholder(mProfileFirstView, mProfileFirst.getIcon());
                mProfileFirstView.setTag(R.id.material_drawer_profile_header, mProfileFirst);
//                if (mProfileImagesClickable) {
//                    mProfileFirstView.setOnClickListener(onProfileClickListener);
//                    mProfileFirstView.setOnLongClickListener(onProfileLongClickListener);
//                    mProfileFirstView.disableTouchFeedback(false);
//                } else {
//                    mProfileFirstView.disableTouchFeedback(true);
//                }
                mProfileFirstView.setVisibility(View.VISIBLE);
                mProfileFirstView.invalidate();
            }
            if (mProfileSecond != null && mProfileImagesVisible && !mOnlyMainProfileImageVisible) {
                setImageOrPlaceholder(mProfileSecondView, mProfileSecond.getIcon());
                mProfileSecondView.setTag(R.id.material_drawer_profile_header, mProfileSecond);
//                if (mProfileImagesClickable) {
//                    mProfileSecondView.setOnClickListener(onProfileClickListener);
//                    mProfileSecondView.setOnLongClickListener(onProfileLongClickListener);
//                    mProfileSecondView.disableTouchFeedback(false);
//                } else {
//                    mProfileSecondView.disableTouchFeedback(true);
//                }
                mProfileSecondView.setVisibility(View.VISIBLE);
                mProfileSecondView.invalidate();
            }
            if (mProfileThird != null && mThreeSmallProfileImages && mProfileImagesVisible && !mOnlyMainProfileImageVisible) {
                setImageOrPlaceholder(mProfileThirdView, mProfileThird.getIcon());
                mProfileThirdView.setTag(R.id.material_drawer_profile_header, mProfileThird);
//                if (mProfileImagesClickable) {
//                    mProfileThirdView.setOnClickListener(onProfileClickListener);
//                    mProfileThirdView.setOnLongClickListener(onProfileLongClickListener);
//                    mProfileThirdView.disableTouchFeedback(false);
//                } else {
//                    mProfileThirdView.disableTouchFeedback(true);
//                }
                mProfileThirdView.setVisibility(View.VISIBLE);
                mProfileThirdView.invalidate();
            }
        } else if (mProfiles != null && mProfiles.size() > 0) {
            IProfile profile = mProfiles.get(0);
            mAccountHeaderTextSection.setTag(R.id.material_drawer_profile_header, profile);
            mAccountHeaderTextSection.setVisibility(View.VISIBLE);
            handleSelectionView(mCurrentProfile, true);
            mAccountSwitcherArrow.setVisibility(View.VISIBLE);
            if (mCurrentProfile != null) {
                StringHolder.applyTo(mCurrentProfile.getName(), mCurrentProfileName);
                StringHolder.applyTo(mCurrentProfile.getEmail(), mCurrentProfileEmail);
            }
        }

        if (!mSelectionFirstLineShown) {
            mCurrentProfileName.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(mSelectionFirstLine)) {
            mCurrentProfileName.setText(mSelectionFirstLine);
            mAccountHeaderTextSection.setVisibility(View.VISIBLE);
        }
        if (!mSelectionSecondLineShown) {
            mCurrentProfileEmail.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(mSelectionSecondLine)) {
            mCurrentProfileEmail.setText(mSelectionSecondLine);
            mAccountHeaderTextSection.setVisibility(View.VISIBLE);
        }

        //if we disabled the list
        if (!mSelectionListEnabled || !mSelectionListEnabledForSingleProfile && mProfileFirst == null && (mProfiles == null || mProfiles.size() == 1)) {
            mAccountSwitcherArrow.setVisibility(View.GONE);
            handleSelectionView(null, false);

            //if we are not in compact mode minimize the padding to make use of the space
            if (!mCompactStyle) {
                mAccountHeaderTextSection.setPadding(0, 0, (int) UIUtils.convertDpToPixel(16, mAccountHeaderTextSection.getContext()), 0);
            }
        }

        //if we disabled the list but still have set a custom listener
        if (mOnAccountHeaderSelectionViewClickListener != null) {
            handleSelectionView(mCurrentProfile, true);
        }
    }

    /**
     * a small helper to handle the selectionView
     *
     * @param on
     */
    private void handleSelectionView(IProfile profile, boolean on) {
        if (on) {
            if (Build.VERSION.SDK_INT >= 21) {
                ((FrameLayout) mAccountHeaderContainer).setForeground(AppCompatResources.getDrawable(mAccountHeaderContainer.getContext(), mAccountHeaderTextSectionBackgroundResource));
                mAccountHeaderContainer.setOnClickListener(onSelectionClickListener);
                mAccountHeaderContainer.setTag(R.id.material_drawer_profile_header, profile);
            } else {
                mAccountHeaderTextSection.setBackgroundResource(mAccountHeaderTextSectionBackgroundResource);
                mAccountHeaderTextSection.setOnClickListener(onSelectionClickListener);
                mAccountHeaderTextSection.setTag(R.id.material_drawer_profile_header, profile);
            }
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                ((FrameLayout) mAccountHeaderContainer).setForeground(null);
                mAccountHeaderContainer.setOnClickListener(null);
            } else {
                UIUtils.setBackground(mAccountHeaderTextSection, null);
                mAccountHeaderTextSection.setOnClickListener(null);
            }
        }
    }
    /**
     * onSelectionClickListener to notify the onClick on the checkbox
     */
    private View.OnClickListener onSelectionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean consumed = false;
            if (mOnAccountHeaderSelectionViewClickListener != null) {
                consumed = mOnAccountHeaderSelectionViewClickListener.onClick(v, (IProfile) v.getTag(R.id.material_drawer_profile_header));
            }

            if (mAccountSwitcherArrow.getVisibility() == View.VISIBLE && !consumed) {
                toggleSelectionList(v.getContext());
            }
        }
    };

    /**
     * helper method to set the height for the header!
     *
     * @param height
     */
    private void setHeaderHeight(int height) {
        if (mAccountHeaderContainer != null) {
            ViewGroup.LayoutParams params = mAccountHeaderContainer.getLayoutParams();
            if (params != null) {
                params.height = height;
                mAccountHeaderContainer.setLayoutParams(params);
            }

            View accountHeader = mAccountHeaderContainer.findViewById(R.id.material_drawer_account_header);
            if (accountHeader != null) {
                params = accountHeader.getLayoutParams();
                params.height = height;
                accountHeader.setLayoutParams(params);
            }

            View accountHeaderBackground = mAccountHeaderContainer.findViewById(R.id.material_drawer_account_header_background);
            if (accountHeaderBackground != null) {
                params = accountHeaderBackground.getLayoutParams();
                params.height = height;
                accountHeaderBackground.setLayoutParams(params);
            }
        }
    }

    /**
     * small helper method to set an profile image or a placeholder
     *
     * @param iv
     * @param imageHolder
     */
    private void setImageOrPlaceholder(ImageView iv, ImageHolder imageHolder) {
        //cancel previous started image loading processes
        DrawerImageLoader.getInstance().cancelImage(iv);
        //set the placeholder
        iv.setImageDrawable(DrawerImageLoader.getInstance().getImageLoader().placeholder(iv.getContext(), DrawerImageLoader.Tags.PROFILE.name()));
        //set the real image (probably also the uri)
        ImageHolder.applyTo(imageHolder, iv, DrawerImageLoader.Tags.PROFILE.name());
    }
}
