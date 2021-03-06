package com.spectralogic.dsl.models

import com.spectralogic.ds3client.commands.spectrads3.PutBucketSpectraS3Request
import com.spectralogic.ds3client.networking.NetworkClient
import com.spectralogic.ds3client.commands.GetBucketRequest
import com.spectralogic.ds3client.commands.GetServiceRequest
import com.spectralogic.ds3client.Ds3ClientImpl

/** Represents a BlackPearl Client */
class BpClient extends Ds3ClientImpl {

    BpClient(NetworkClient netClient) {
        super(netClient)
    }

    /** @return BpBucket of bucket with given name  */
    BpBucket bucket(String bucketName) {
        def response = this.getBucket(new GetBucketRequest(bucketName))
        return new BpBucket(response, this)
    }

    /** @return the names of the buckets  */
    List<String> buckets() {
        def response = this.getService(new GetServiceRequest())
        return response.getListAllMyBucketsResult().getBuckets().collect { it.getName() }
    }

    /** @return newly created BpBucket  */
    BpBucket createBucket(String name, String dataPolicyId = "") {
        putBucketSpectraS3(new PutBucketSpectraS3Request(name).withDataPolicyId(dataPolicyId))
        return bucket(name)
    }

    String toString() {
        def details = this.netClient.getConnectionDetails()
        "endpoint: ${details.getEndpoint()}, " +
                "access_key: ${details.getCredentials().getClientId()}"
    }

}
