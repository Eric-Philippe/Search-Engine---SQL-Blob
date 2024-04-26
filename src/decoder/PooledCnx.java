package apple.util.decoder;

import apple.util.dummyClasses.ConnectionData;

public class PooledCnx implements AutoCloseable{
	private ConnectionData _cnxdata;
	
	public PooledCnx()  {
		ConnectionData cnxdata=ConnectionFactory.createConnectionReadyToUse();
		cnxdata.openDbAccess();
		_cnxdata=cnxdata;
	}
	
	/**
	 * Retourne le ConnectionData de ce PooledCnx. Le principe et qu'on retourne toujours un ConnectionData ouvert.
	 * @return
	 */
	public ConnectionData getCnxData()  {
		if (_cnxdata.isClosed())
			_cnxdata.openDbAccess();
		return _cnxdata;
	}

	/**
	 * Rend au ConnectionPool la connection. Elle reste ouverte.  :-(
	 */
	@Override
	public void close() {
		ConnectionPool.giveBackConnection(this);
	}

	public boolean isOpen() {
		return _cnxdata.isOpened();
	}

	@Override
	public String toString() {
		return "PooledCnx [_cnxdata=" + _cnxdata + "]";
	}
	
}