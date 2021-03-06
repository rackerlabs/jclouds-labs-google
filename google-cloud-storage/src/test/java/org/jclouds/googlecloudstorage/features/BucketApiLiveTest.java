/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.googlecloudstorage.features;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jclouds.googlecloud.domain.ListPage;
import org.jclouds.googlecloudstorage.domain.Bucket;
import org.jclouds.googlecloudstorage.domain.Bucket.Cors;
import org.jclouds.googlecloudstorage.domain.Bucket.Logging;
import org.jclouds.googlecloudstorage.domain.Bucket.Versioning;
import org.jclouds.googlecloudstorage.domain.BucketAccessControls;
import org.jclouds.googlecloudstorage.domain.BucketAccessControls.Role;
import org.jclouds.googlecloudstorage.domain.DomainResourceReferences.Location;
import org.jclouds.googlecloudstorage.domain.DomainResourceReferences.ObjectRole;
import org.jclouds.googlecloudstorage.domain.DomainResourceReferences.Projection;
import org.jclouds.googlecloudstorage.domain.DomainResourceReferences.StorageClass;
import org.jclouds.googlecloudstorage.domain.ObjectAccessControls;
import org.jclouds.googlecloudstorage.domain.templates.BucketTemplate;
import org.jclouds.googlecloudstorage.internal.BaseGoogleCloudStorageApiLiveTest;
import org.jclouds.googlecloudstorage.options.DeleteBucketOptions;
import org.jclouds.googlecloudstorage.options.GetBucketOptions;
import org.jclouds.googlecloudstorage.options.InsertBucketOptions;
import org.jclouds.googlecloudstorage.options.UpdateBucketOptions;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class BucketApiLiveTest extends BaseGoogleCloudStorageApiLiveTest {

   private static final String BUCKET_NAME = "jcloudstestbucket" + (int) (Math.random() * 10000);

   private static final String BUCKET_NAME_WITHOPTIONS = "jcloudtestbucketoptions" + (int) (Math.random() * 10000);

   private static final String LOG_BUCKET_NAME = "jcloudslogbucket" + (int) (Math.random() * 10000);

   private Long metageneration;

   private BucketApi api() {
      return api.getBucketApi();
   }

   @Test(groups = "live")
   public void testCreateBucket() {

      BucketTemplate logTemplate = new BucketTemplate().name(LOG_BUCKET_NAME);
      Bucket logResponse = api().createBucket(PROJECT_NUMBER, logTemplate);
      assertNotNull(logResponse);

      BucketAccessControls acl = BucketAccessControls.builder().bucket(BUCKET_NAME).entity("allUsers").role(Role.OWNER)
               .build();
      ObjectAccessControls oac = ObjectAccessControls.builder().bucket(BUCKET_NAME).entity("allUsers")
               .role(ObjectRole.OWNER).build();
      Cors cors = Cors.create(Arrays.asList("http://example.appspot.com"), Arrays.asList("GET", "HEAD"),
            Arrays.asList("x-meta-goog-custom"), 10);
      Versioning version = Versioning.create(true);

      Logging log = Logging.create(LOG_BUCKET_NAME, BUCKET_NAME);

      BucketTemplate template = new BucketTemplate().name(BUCKET_NAME).addAcl(acl).addDefaultObjectAccessControls(oac)
               .versioning(version).location(Location.US_CENTRAL2).logging(log)
               .storageClass(StorageClass.DURABLE_REDUCED_AVAILABILITY).addCORS(cors);

      Bucket response = api().createBucket(PROJECT_NUMBER, template);

      assertNotNull(response);
      assertNotNull(response.cors());
      assertTrue(response.cors().size() == 1);
      assertEquals(response.name(), BUCKET_NAME);
      assertEquals(response.location(), Location.US_CENTRAL2);
      assertTrue(response.versioning().enabled());
   }

   @Test(groups = "live", dependsOnMethods = { "testCreateBucket" })
   public void testCreateAlreadyExistBucket() {

      BucketTemplate template = new BucketTemplate().name(BUCKET_NAME).location(Location.US_CENTRAL2)
               .storageClass(StorageClass.DURABLE_REDUCED_AVAILABILITY);

      assertNull(api().createBucket(PROJECT_NUMBER, template));
   }

   @Test(groups = "live")
   public void testCreateBucketWithOptions() {
      ObjectAccessControls oac = ObjectAccessControls.builder().bucket(BUCKET_NAME_WITHOPTIONS)
               .entity("allUsers").role(ObjectRole.OWNER).build();
      Cors cors = Cors.create(Arrays.asList("http://example.appspot.com"), Arrays.asList("GET", "HEAD"),
            Arrays.asList("x-meta-goog-custom"), 10);
      Versioning version = Versioning.create(true);

      BucketTemplate template = new BucketTemplate().name(BUCKET_NAME_WITHOPTIONS).addDefaultObjectAccessControls(oac)
               .versioning(version).location(Location.US_CENTRAL2)
               .storageClass(StorageClass.DURABLE_REDUCED_AVAILABILITY).addCORS(cors);

      InsertBucketOptions options = new InsertBucketOptions().projection(Projection.FULL);

      Bucket response = api().createBucket(PROJECT_NUMBER, template, options);

      assertNotNull(response);
      assertNotNull(response.cors());
      assertEquals(response.name(), BUCKET_NAME_WITHOPTIONS);
      assertEquals(response.location(), Location.US_CENTRAL2);
      assertTrue(response.versioning().enabled());
   }

   @Test(groups = "live", dependsOnMethods = "testCreateBucket")
   public void testUpdateBucket() {
      BucketAccessControls bucketacl = BucketAccessControls.builder().bucket(BUCKET_NAME)
               .entity("allAuthenticatedUsers").role(Role.OWNER).build();
      BucketTemplate template = new BucketTemplate().name(BUCKET_NAME).addAcl(bucketacl);
      Bucket response = api().updateBucket(BUCKET_NAME, template);

      assertNotNull(response);
      assertEquals(response.name(), BUCKET_NAME);
      assertNotNull(response.acl());
   }

   @Test(groups = "live", dependsOnMethods = "testCreateBucketWithOptions")
   public void testUpdateBucketWithOptions() {
      BucketAccessControls bucketacl = BucketAccessControls.builder().bucket(BUCKET_NAME_WITHOPTIONS)
               .entity("allAuthenticatedUsers").role(Role.OWNER).build();
      UpdateBucketOptions options = new UpdateBucketOptions().projection(Projection.FULL);
      BucketTemplate template = new BucketTemplate().name(BUCKET_NAME_WITHOPTIONS).addAcl(bucketacl);
      Bucket response = api().updateBucket(BUCKET_NAME_WITHOPTIONS, template, options);

      assertNotNull(response);

      metageneration = response.metageneration();

      assertEquals(response.name(), BUCKET_NAME_WITHOPTIONS);
      assertNotNull(response.acl());
   }

   @Test(groups = "live", dependsOnMethods = "testCreateBucket")
   public void testGetBucket() {
      Bucket response = api().getBucket(BUCKET_NAME);

      assertNotNull(response);
      assertEquals(response.name(), BUCKET_NAME);
   }

   @Test(groups = "live", dependsOnMethods = "testUpdateBucketWithOptions")
   public void testGetBucketWithOptions() {
      GetBucketOptions options = new GetBucketOptions().ifMetagenerationMatch(metageneration);
      Bucket response = api().getBucket(BUCKET_NAME_WITHOPTIONS, options);

      assertNotNull(response);
      assertEquals(response.name(), BUCKET_NAME_WITHOPTIONS);
   }

   @Test(groups = "live", dependsOnMethods = "testCreateBucket")
   public void testListBucket() {
      ListPage<Bucket> bucket = api().listBucket(PROJECT_NUMBER);

      Iterator<Bucket> pageIterator = bucket.iterator();
      assertTrue(pageIterator.hasNext());

      Bucket iteratedBucket = pageIterator.next();
      List<Bucket> bucketAsList = Lists.newArrayList(iteratedBucket);

      assertNotNull(iteratedBucket);
      assertSame(bucketAsList.size(), 1);
   }

   @Test(groups = "live", dependsOnMethods = "testCreateBucket")
   public void testPatchBucket() {
      Logging logging = Logging.create(LOG_BUCKET_NAME, BUCKET_NAME);
      BucketTemplate template = new BucketTemplate().name(BUCKET_NAME).logging(logging);

      Bucket response = api().patchBucket(BUCKET_NAME, template);

      assertNotNull(response);
      assertEquals(response.name(), BUCKET_NAME);
      assertEquals(response.logging().logBucket(), LOG_BUCKET_NAME);
   }

   @Test(groups = "live", dependsOnMethods = { "testListBucket", "testGetBucket", "testUpdateBucket" })
   public void testDeleteBucket() {
      assertTrue(api().deleteBucket(BUCKET_NAME));
      assertTrue(api().deleteBucket(LOG_BUCKET_NAME));
   }

   @Test(groups = "live", dependsOnMethods = { "testDeleteBucket" })
   public void testDeleteNotExistingBucket() {
      assertTrue(api().deleteBucket(BUCKET_NAME));
   }

   @Test(groups = "live", dependsOnMethods = { "testGetBucketWithOptions" })
   public void testDeleteBucketWithOptions() {

      DeleteBucketOptions options = new DeleteBucketOptions().ifMetagenerationMatch(metageneration)
               .ifMetagenerationNotMatch(metageneration + 1);

      api().deleteBucket(BUCKET_NAME_WITHOPTIONS, options);

   }
}
