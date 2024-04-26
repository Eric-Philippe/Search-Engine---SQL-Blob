package apple.util.rechercherExpression;

public class RegleResultat implements Comparable<RegleResultat> {
	private String _modeleId;
	private String _versionId;
	private String _projetId;
	private String _clientId;
	private String _regleId;
	private String _privateKey;
	private boolean _isFromApple;
	
	public RegleResultat(String regleId, boolean isFromApple) {
		if (regleId.contains("#")) {
			String[] c = regleId.split("#");
			this._regleId = c[2];
			this._projetId = c[1];
			this._clientId = c[0];
		} else {
			this._regleId = regleId;
		}
		this._isFromApple = isFromApple;
		this.setPrivateKey();
	}
	
	public RegleResultat(String regleId, String projetId, String clientId, boolean isFromApple) {
		this._regleId = regleId;
		this._projetId = projetId;
		this._clientId = clientId;
		this._isFromApple = isFromApple;
		this.setPrivateKey();
	}
	
	public RegleResultat(String regleId, String modeleId, String versionId, String projetId, boolean isFromApple) {
		this._regleId = regleId;
		this._modeleId = modeleId;
		this._versionId = versionId;
		this._projetId = projetId;
		this._isFromApple = isFromApple;
		this.setPrivateKey();
	}
	
	public RegleResultat(String regleId, String modeleId, String versionId, String projetId, String clientId, boolean isFromApple) {
		this._regleId = regleId;
		this._modeleId = modeleId;
		this._versionId = versionId;
		this._projetId = projetId;
		this._clientId = clientId;
		this._isFromApple = isFromApple;
		this.setPrivateKey();
	}
	
	private void setPrivateKey() {
		if (this._clientId != null) this._privateKey = this._clientId + this._projetId + this._regleId;
		else this._privateKey = this._regleId;
	}
	
	public String getRegleId() {
		return this._regleId;
	}
	
	protected String getPrivateKey() {
		return this._privateKey;
	}
	
	public String getModelId() {
		return this._modeleId;
	}
	
	public String getVersionId() {
		return this._versionId;
	}
	
	public String getProjetId() {
		return this._projetId;
	}
	
	public String getClientId() {
		return this._clientId;
	}
	
	public boolean isFromApple() {
		return this._isFromApple;
	}
	
	@Override
	public int hashCode() {
		return this._privateKey.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		RegleResultat myResultat = (RegleResultat) obj;
		return this._privateKey.equals(myResultat._privateKey);
	}

	@Override
	public int compareTo(RegleResultat o) {
		return this._privateKey.compareTo(o.getPrivateKey());
	}
}
