package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

// texture a voxel with a given texture (id)
final class TextureVoxelIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Voxel voxel;
    private final int[] oldVoxelTexture;
    private final int[] newVoxelTexture;

    protected TextureVoxelIntent(VoxelData voxelData, int voxelId, Integer voxelSide, int newTextureId, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.voxel = this.voxelData.dataContainer.voxels.get(voxelId);
        this.oldVoxelTexture = voxel.getTexture();
        if (newTextureId != -1) { // otherwise unset texture
            if (oldVoxelTexture == null || voxelSide == null) {
                newVoxelTexture = new int[] {
                        newTextureId, newTextureId, newTextureId,
                        newTextureId, newTextureId, newTextureId
                };
            } else {
                newVoxelTexture = oldVoxelTexture.clone();
                newVoxelTexture[voxelSide] = newTextureId;
            }
        } else {
            newVoxelTexture = null;
        }
        // what is effected
        effected = new int[][]{voxel.getPosAsInt()};
    }

    @Override
    protected void applyAction() {
        voxel.setTexture(newVoxelTexture);
    }

    @Override
    protected void unapplyAction() {
        voxel.setTexture(oldVoxelTexture);
    }

    private int[][] effected = null;
    @Override
    public int[][] effected() {
        return effected;
    }
}