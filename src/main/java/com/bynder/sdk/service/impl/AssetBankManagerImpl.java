/**
 * Copyright (c) 2017 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.bynder.sdk.api.BynderApi;
import com.bynder.sdk.model.Brand;
import com.bynder.sdk.model.DownloadUrl;
import com.bynder.sdk.model.Media;
import com.bynder.sdk.model.Metaproperty;
import com.bynder.sdk.model.Tag;
import com.bynder.sdk.query.AddMetapropertyToMediaQuery;
import com.bynder.sdk.query.MediaDownloadQuery;
import com.bynder.sdk.query.MediaInfoQuery;
import com.bynder.sdk.query.MediaPropertiesQuery;
import com.bynder.sdk.query.MediaQuery;
import com.bynder.sdk.query.MetapropertyQuery;
import com.bynder.sdk.query.UploadQuery;
import com.bynder.sdk.service.AssetBankManager;
import com.bynder.sdk.service.exception.BynderUploadException;
import com.bynder.sdk.service.upload.FileUploader;
import com.bynder.sdk.util.Utils;

import io.reactivex.Observable;
import retrofit2.Response;

public class AssetBankManagerImpl implements AssetBankManager {

    private final BynderApi bynderApi;
    private final FileUploader fileUploader;

    public AssetBankManagerImpl(final BynderApi bynderApi) {
        this.bynderApi = bynderApi;
        this.fileUploader = FileUploader.create(bynderApi);
    }

    @Override
    public Observable<Response<List<Brand>>> getBrands() {
        return bynderApi.getBrands();
    }

    @Override
    public Observable<Response<List<Tag>>> getTags() {
        return bynderApi.getTags();
    }

    @Override
    public Observable<Response<Map<String, Metaproperty>>> getMetaproperties(final MetapropertyQuery metapropertyQuery) {
        return bynderApi.getMetaproperties(metapropertyQuery.getCount());
    }

    @Override
    public Observable<Response<List<Media>>> getMediaList(final MediaQuery mediaQuery) {
        return bynderApi.getMediaList(mediaQuery.getType() == null ? null : mediaQuery.getType().toString(), mediaQuery.getKeyword(), mediaQuery.getLimit(), mediaQuery.getPage(),
                StringUtils.join(mediaQuery.getPropertyOptionId(), Utils.STR_COMMA), mediaQuery.getCount());
    }

    @Override
    public Observable<Response<Media>> getMediaInfo(final MediaInfoQuery mediaInfoQuery) {
        return bynderApi.getMediaInfo(mediaInfoQuery.getMediaId(), mediaInfoQuery.getVersions());
    }

    @Override
    public Observable<Response<DownloadUrl>> getMediaDownloadUrl(final MediaDownloadQuery mediaDownloadQuery) {
        if (mediaDownloadQuery.getMediaItemId() == null) {
            return bynderApi.getMediaDownloadUrl(mediaDownloadQuery.getMediaId());
        } else {
            return bynderApi.getMediaDownloadUrl(mediaDownloadQuery.getMediaId(), mediaDownloadQuery.getMediaItemId());
        }
    }

    @Override
    public Observable<Response<Void>> setMediaProperties(final MediaPropertiesQuery mediaPropertiesQuery) {
        return bynderApi.setMediaProperties(mediaPropertiesQuery.getMediaId(), mediaPropertiesQuery.getName(), mediaPropertiesQuery.getDescription(), mediaPropertiesQuery.getCopyright(),
                mediaPropertiesQuery.getArchive(), mediaPropertiesQuery.getDatePublished());
    }

    @Override
    public Observable<Response<Void>> addMetapropertyToMedia(final AddMetapropertyToMediaQuery addMetapropertyToMediaQuery) {
        Map<String, String> metapropertyOptions = new HashMap<>();
        metapropertyOptions.put(String.format("metaproperty.%s", addMetapropertyToMediaQuery.getMetapropertyId()), StringUtils.join(addMetapropertyToMediaQuery.getOptionsIds(), Utils.STR_COMMA));

        return bynderApi.addMetapropertyToMedia(addMetapropertyToMediaQuery.getMediaId(), metapropertyOptions);
    }

    @Override
    public void uploadFile(final UploadQuery uploadQuery) throws BynderUploadException, IOException, InterruptedException, RuntimeException {
        fileUploader.uploadFile(uploadQuery);
    }
}
