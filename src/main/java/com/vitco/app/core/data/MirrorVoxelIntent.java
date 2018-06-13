package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class MirrorVoxelIntent extends VoxelActionIntent  {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Voxel[] voxels;
    private final int axe;

    protected MirrorVoxelIntent(VoxelData voxelData, Voxel[] voxels, int axe, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.voxels = voxels;
        this.axe = axe;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {

            // what is effected (there could be duplicate positions here)
            effected = new int[voxels.length*2][];

            // generate the ids and find center
            int[] centerMin = null;
            int[] centerMax = null;
            Integer[] voxelIds = new Integer[voxels.length];
            for (int i = 0; i < voxels.length; i++) {
                voxelIds[i] = voxels[i].id;
                Voxel voxel = voxels[i];
                if (centerMin == null) {
                    centerMin = voxel.getPosAsInt();
                    centerMax = voxel.getPosAsInt();
                }
                centerMin[0] = Math.min(centerMin[0],voxel.x);
                centerMin[1] = Math.min(centerMin[1],voxel.y);
                centerMin[2] = Math.min(centerMin[2],voxel.z);
                centerMax[0] = Math.max(centerMax[0],voxel.x);
                centerMax[1] = Math.max(centerMax[1],voxel.y);
                centerMax[2] = Math.max(centerMax[2],voxel.z);
            }
            // calculate center - note: voxels.length must not be zero
            assert centerMin != null;
            float[] center = new float[] {
                    (centerMin[0]/(float)2 + centerMax[0]/(float)2),
                    (centerMin[1]/(float)2 + centerMax[1]/(float)2),
                    (centerMin[2]/(float)2 + centerMax[2]/(float)2)
            };

            // remove all voxels
            this.voxelData.historyManagerV.applyIntent(new MassRemoveVoxelIntent(this.voxelData, voxelIds, true));

            // create new voxels (with new position) and delete
            // existing voxels at those positions
            Voxel[] shiftedVoxels = new Voxel[voxels.length];
            for (int i = 0; i < voxels.length; i++) {
                Voxel voxel = voxels[i];
                int[] pos = voxel.getPosAsInt();
                effected[i] = voxel.getPosAsInt(); // what is effected

                // switch the point with the center
                pos[axe] = Math.round(- pos[axe] + 2*center[axe]);

                effected[i + voxels.length] = pos.clone(); // what is effected
                shiftedVoxels[i] = new Voxel(voxel.id, pos, voxel.getColor(), voxel.isSelected(), voxel.getTexture(), voxel.getLayerId());
                // remove existing voxels in this layer
                Voxel result = this.voxelData.dataContainer.layers.get(voxel.getLayerId()).search(pos);
                if (result != null) {
                    this.voxelData.historyManagerV.applyIntent(new RemoveVoxelIntent(this.voxelData, result.id, true));
                }
            }
            // (re)add all the rotated voxels (null ~ the voxel layer id is used)
            this.voxelData.historyManagerV.applyIntent(new MassAddVoxelIntent(this.voxelData, shiftedVoxels, null, true));
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