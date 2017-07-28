package com.rubenwardy.minetestmodmanager.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.rubenwardy.minetestmodmanager.R;
import com.rubenwardy.minetestmodmanager.models.Mod;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class SectionedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements SectionTitleProvider {

    @NonNull
    private final Context mContext;
    private static final int SECTION_TYPE = 0;

    private boolean mValid = true;
    private final int mSectionResourceId;
    private final int mTextResourceId;

    private final RecyclerView.Adapter mBaseAdapter;
    @NonNull
    private SparseArray<Section> mSections = new SparseArray<>();

    public void setMods(List<Mod> mods) {
        ((ModListActivity.ModListRecyclerViewAdapter)mBaseAdapter).setMods(mods);
    }


    public SectionedRecyclerViewAdapter(@NonNull Context context, @LayoutRes int sectionResourceId,
                                        @IdRes int textResourceId, @NonNull RecyclerView.Adapter baseAdapter) {

        mSectionResourceId = sectionResourceId;
        mTextResourceId = textResourceId;
        mBaseAdapter = baseAdapter;
        mContext = context;

        mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }

    @Override
    public String getSectionTitle(int position) {
        if (isSectionHeaderPosition(position)) {
            for (int i = 0; i < 2 && i + position < getItemCount(); i++) {
                if (!isSectionHeaderPosition(position + i)) {
                    return ((SectionTitleProvider)mBaseAdapter).getSectionTitle(sectionedPositionToPosition(position + i));
                }
            }

            for (int i = 0; i < 2 && position - i - 1 >= 0; i++) {
                if (!isSectionHeaderPosition(position - i - 1)) {
                    return ((SectionTitleProvider)mBaseAdapter).getSectionTitle(sectionedPositionToPosition(position - i - 1));
                }
            }

            return "?";
        } else {
            return ((SectionTitleProvider)mBaseAdapter).getSectionTitle(sectionedPositionToPosition(position));
        }
    }


    public static class SectionViewHolder extends RecyclerView.ViewHolder {

        public final TextView title;
        public final View configModsBtn;

        public SectionViewHolder(@NonNull View view, int mTextResourceid) {
            super(view);
            title = (TextView) view.findViewById(mTextResourceid);
            configModsBtn = view.findViewById(R.id.world);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int typeView) {
        if (typeView == SECTION_TYPE) {
            final View view = LayoutInflater.from(mContext).inflate(mSectionResourceId, parent, false);
            return new SectionViewHolder(view, mTextResourceId);
        }else{
            return mBaseAdapter.onCreateViewHolder(parent, typeView -1);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder sectionViewHolder, int position) {
        if (isSectionHeaderPosition(position)) {
            Section sec = mSections.get(position);
            SectionViewHolder holder = ((SectionViewHolder) sectionViewHolder);
            holder.title.setText(sec.title);
            final CharSequence worlds = sec.worlds;
            if (worlds == null || worlds.equals("")) {
                holder.configModsBtn.setVisibility(View.GONE);
            } else {
                holder.configModsBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, WorldConfigActivity.class);
                        context.startActivity(intent);                    }
                });
            }
        } else if (mBaseAdapter != null) {
            //noinspection unchecked
            mBaseAdapter.onBindViewHolder(sectionViewHolder, sectionedPositionToPosition(position));
        }

    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position)
                ? SECTION_TYPE
                : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) +1 ;
    }


    public static class Section {
        final int firstPosition;
        int sectionedPosition;
        final CharSequence title;
        final CharSequence worlds;

        public Section(int firstPosition, CharSequence title, CharSequence worlds) {
            this.firstPosition = firstPosition;
            this.title = title;
            this.worlds = worlds;
        }
    }


    public void setSections(@NonNull Section[] sections) {
        mSections.clear();

        Arrays.sort(sections, new Comparator<Section>() {
            @Override
            public int compare(@NonNull Section o, @NonNull Section o1) {
                return (o.firstPosition == o1.firstPosition)
                        ? 0
                        : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
            }
        });

        int offset = 0; // offset positions for the headers we're adding
        for (Section section : sections) {
            section.sectionedPosition = section.firstPosition + offset;
            mSections.append(section.sectionedPosition, section);
            ++offset;
        }

        notifyDataSetChanged();
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).firstPosition > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    private int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    private boolean isSectionHeaderPosition(int position) {
        return mSections.get(position) != null;
    }


    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position)
                ? Integer.MAX_VALUE - mSections.indexOfKey(position)
                : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
    }

    @Override
    public int getItemCount() {
        return (mValid ? mBaseAdapter.getItemCount() + mSections.size() : 0);
    }

}