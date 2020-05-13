package com.lzq.tuliaoand.common;

import com.blankj.utilcode.util.SPUtils;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.MapTileIndex;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class CommonLayers {

    //矢量底图 _W是墨卡托投影  _c是国家2000的坐标系
    private static final String tdt_vec = "tdt_vec";
    public static final OnlineTileSourceBase tianDiTuVecTileSource = new OnlineTileSourceBase(tdt_vec, 2, 20, 256, "",
            new String[]{"http://t1.tianditu.com/DataServer?T=img_w&tk=f75b6af65f9943541be7e2e6a830c4ea",
                    "http://t2.tianditu.com/DataServer?T=img_w&tk=f75b6af65f9943541be7e2e6a830c4ea",
                    "http://t3.tianditu.com/DataServer?T=img_w&tk=f75b6af65f9943541be7e2e6a830c4ea",
                    "http://t4.tianditu.com/DataServer?T=img_w&tk=f75b6af65f9943541be7e2e6a830c4ea",
                    "http://t5.tianditu.com/DataServer?T=img_w&tk=f75b6af65f9943541be7e2e6a830c4ea",
                    "http://t6.tianditu.com/DataServer?T=img_w&tk=f75b6af65f9943541be7e2e6a830c4ea"}) {
        @Override
        public String getTileURLString(final long pMapTileIndex) {
            return getBaseUrl() + "&X=" + MapTileIndex.getX(pMapTileIndex) + "&Y=" + MapTileIndex.getY(pMapTileIndex)
                    + "&L=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };

    //矢量标注 _W是墨卡托投影  _c是国家2000的坐标系
    private static final String tdt_cva = "tdt_cva";
    public static final OnlineTileSourceBase tianDiTuCvaTileSource = new OnlineTileSourceBase(tdt_cva, 2, 20, 256, "",
            new String[]{"http://t1.tianditu.com/DataServer?T=cva_w&tk=f75b6af65f9943541be7e2e6a830c4ea",
                    "http://t2.tianditu.com/DataServer?T=cva_w&tk=f75b6af65f9943541be7e2e6a830c4ea",
                    "http://t3.tianditu.com/DataServer?T=cva_w&tk=f75b6af65f9943541be7e2e6a830c4ea",
                    "http://t4.tianditu.com/DataServer?T=cva_w&tk=f75b6af65f9943541be7e2e6a830c4ea",
                    "http://t5.tianditu.com/DataServer?T=cva_w&tk=f75b6af65f9943541be7e2e6a830c4ea",
                    "http://t6.tianditu.com/DataServer?T=cva_w&tk=f75b6af65f9943541be7e2e6a830c4ea"}) {
        @Override
        public String getTileURLString(final long pMapTileIndex) {
            return getBaseUrl() + "&X=" + MapTileIndex.getX(pMapTileIndex) + "&Y=" + MapTileIndex.getY(pMapTileIndex)
                    + "&L=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };

    //

}
