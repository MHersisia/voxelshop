package com.vitco.app.core.data;

import com.vitco.app.core.data.history.VoxelActionIntent;

// texture many voxel at the same time
final class MassTextureVoxelIntent extends VoxelActionIntent  {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Integer[] voxelIds;
    private final int textureId;

    protected MassTextureVoxelIntent(VoxelData voxelData, Integer[] voxelIds, int textureId, boolean attach) {
        super(attach);
		this.voxelData = voxelData;

        // what is effected (there could be duplicate positions here)
        effected = new int[voxelIds.length][];
        for (int i = 0; i < effected.length; i++) {
            effected[i] = this.voxelData.dataContainer.voxels.get(voxelIds[i]).getPosAsInt();
        }

        this.voxelIds = voxelIds;
        this.textureId = textureId;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            for (Integer voxelId : voxelIds) {
                this.voxelData.historyManagerV.applyIntent(new TextureVoxelIntent(this.voxelData, voxelId, null, textureId, true));
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