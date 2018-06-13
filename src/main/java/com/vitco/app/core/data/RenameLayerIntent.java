package com.vitco.app.core.data;

import com.vitco.app.core.data.history.VoxelActionIntent;

final class RenameLayerIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Integer layerId;
    private final String newName;
    private String oldName;

    protected RenameLayerIntent(VoxelData voxelData, int layerId, String newName, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.layerId = layerId;
        this.newName = newName;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            oldName = this.voxelData.dataContainer.layers.get(layerId).getName();
        }
        this.voxelData.dataContainer.layers.get(layerId).setName(newName);
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.layers.get(layerId).setName(oldName);
    }

    @Override
    public int[][] effected() {
        // nothing effected
        return new int[0][];
    }
}