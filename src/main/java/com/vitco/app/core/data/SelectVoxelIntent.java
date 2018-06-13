package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class SelectVoxelIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final int voxelId;
    private final boolean selected;
    private boolean prevSelected;
    private Voxel voxel;

    protected SelectVoxelIntent(VoxelData voxelData, int voxelId, boolean selected, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.voxelId = voxelId;
        this.selected = selected;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            voxel = this.voxelData.dataContainer.voxels.get(voxelId);
            prevSelected = voxel.isSelected();
        }
        voxel.setSelected(selected);
    }

    @Override
    protected void unapplyAction() {
        voxel.setSelected(prevSelected);
    }

    @Override
    public int[][] effected() {
        return new int[][]{voxel.getPosAsInt()};
    }
}