# Deploy FlowForge On Oracle Cloud Infrastructure

This guide deploys FlowForge to one OCI Compute VM using Docker Compose:

- Public internet traffic enters the `frontend` Nginx container on port `80`.
- Nginx serves the Angular app and proxies `/api` to the private backend container.
- PostgreSQL is private inside Docker and persists data in a Docker volume.

## 1. Wait For Account Setup

If OCI shows `Your account is being set up`, wait for Oracle's setup email before creating resources. Some Compute and Networking actions can be unavailable until that finishes.

## 2. Create The Compute Instance

1. In OCI Console, keep the region as `India West (Mumbai)` unless you prefer another region.
2. Open the navigation menu.
3. Go to `Compute` -> `Instances`.
4. Click `Create instance`.
5. Name it `flowforge-vm`.
6. Image: choose `Ubuntu 24.04` or another current Ubuntu image.
7. Shape: choose an Ampere A1 flexible shape if available, for example `1 OCPU` and `6 GB RAM`.
8. Networking: create or select a VCN with internet connectivity and assign a public IPv4 address.
9. SSH keys: generate a key pair or upload your public key. Download and keep the private key safe.
10. Create the instance and copy its public IP address.

Oracle's Compute docs describe this flow under `Compute` -> `Instances` -> `Create instance`, and note that public IPs are required for internet SSH access.

## 3. Open Network Ports

In the instance's VCN or Network Security Group, add ingress rules:

| Port | Source | Purpose |
| --- | --- | --- |
| `22/tcp` | Your IP address if possible | SSH |
| `80/tcp` | `0.0.0.0/0` | FlowForge web app |
| `443/tcp` | `0.0.0.0/0` | Future HTTPS |

Do not expose `5432` or `8080` publicly.

## 4. SSH Into The VM

From your local machine:

```bash
ssh -i path/to/private-key ubuntu@YOUR_PUBLIC_IP
```

## 5. Install Docker On Ubuntu

Run this on the VM:

```bash
sudo apt update
sudo apt install -y ca-certificates curl git
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
sudo tee /etc/apt/sources.list.d/docker.sources > /dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker
```

Optional, so you can run Docker without `sudo`:

```bash
sudo usermod -aG docker $USER
newgrp docker
```

## 6. Put The Project On The VM

Recommended path after the project is on GitHub:

```bash
git clone https://github.com/YOUR_USERNAME/FlowForge.git
cd FlowForge
```

If the repo is not on GitHub yet, upload a zip/SCP copy of this folder to the VM, then unzip it and `cd` into it.

## 7. Create Production Environment Values

Create a `.env` file on the VM:

```bash
cat > .env <<'EOF'
DB_NAME=flowforge
DB_USER=flowforge
DB_PASSWORD=replace-with-a-strong-database-password
JWT_SECRET=replace-with-a-long-random-jwt-secret-at-least-64-characters
CORS_ALLOWED_ORIGINS=http://YOUR_PUBLIC_IP
EOF
```

Replace `YOUR_PUBLIC_IP`.

## 8. Start FlowForge

```bash
docker compose -f docker-compose.oracle.yml up -d --build
```

Check containers:

```bash
docker compose -f docker-compose.oracle.yml ps
docker compose -f docker-compose.oracle.yml logs -f
```

Open:

```text
http://YOUR_PUBLIC_IP
```

Seeded users:

```text
employee@flowforge.com
manager@flowforge.com
hr@flowforge.com
```

Password:

```text
FlowForge@123
```

## 9. Update Later

If deployed from GitHub:

```bash
cd FlowForge
git pull
docker compose -f docker-compose.oracle.yml up -d --build
```

## Useful References

- OCI Compute instance creation: https://docs.oracle.com/en-us/iaas/Content/Compute/Tasks/launchinginstance.htm
- OCI security lists: https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/securitylists.htm
- Docker Engine on Ubuntu: https://docs.docker.com/engine/install/ubuntu/
