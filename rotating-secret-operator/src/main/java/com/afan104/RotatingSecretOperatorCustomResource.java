package com.afan104;

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.fabric8.kubernetes.model.annotation.Kind;

@Group("com.afan104")
@Version("v1")
@Kind("RotatingSecret")
public class RotatingSecretOperatorCustomResource extends CustomResource<RotatingSecretOperatorSpec,RotatingSecretOperatorStatus> implements Namespaced {
}
