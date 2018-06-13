package com.vitco.app.core.data;

import com.vitco.app.core.data.history.VoxelActionIntent;

final class MassSelectVoxelIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Integer[] voxelIds;
    private final boolean selected;

    protected MassSelectVoxelIntent(VoxelData voxelData, Integer[] voxelIds, boolean selected, boolean attach) {
        super(attach);
		this.voxelData = voxelData;

        // what is effected (there could be duplicate positions here)
        effected = new int[voxelIds.length][];
        for (int i = 0; i < effected.length; i++) {
            effected[i] = this.voxelData.dataContainer.voxels.get(voxelIds[i]).getPosAsInt();
        }

        this.voxelIds = voxelIds;
        this.selected = selected;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            for (Integer id : voxelIds) {
                this.voxelData.historyManagerV.applyIntent(new SelectVoxelIntent(this.voxelData, id, selected, true));
            }
        }
    }

    @Override
    protected void unapplyAction() {
        // nothing to do
    }

    private int[][] effected = null;
    @Override
    public int[][] effected() {
        return effected;
    }
}