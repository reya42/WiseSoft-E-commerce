import React, { useState, useEffect } from 'react';

import { formatDate } from '../utils';
import { useNavigate } from 'react-router-dom';

import "./css/purchases.css"

const Purchases = ({  }) => {
  const [purchases, setPurchases] = useState([]);
  const [loading, setLoading] = useState(true);

  const navigate = useNavigate()
  useEffect(() => {
    if (localStorage.getItem("user") != null)
        {
            const user = JSON.parse(localStorage.getItem("user"));
            const fetchData = async () => {
                try {
                  const response = await fetch(`http://localhost:8080/users/purchases/${user.userId}`);
                  if (!response.ok) {
                    throw new Error('Failed to fetch data');
                  }
                  const data = await response.json();
                  setPurchases(data.content);
                  console.log(data);
                  setLoading(false);
                } catch (error) {
                  console.error('Error fetching data:', error);
                }
              };
              fetchData();
        }
    else
    {
        navigate("/");
    }
  }, [localStorage.getItem("user")]);

  return (
    <div className='purchases_container'>
      <h2>Siparişler</h2>
      {loading ? (
        <p>Loading...</p>
      ) : purchases.length === 0 ? (
        <p>You haven't purchased anything yet.</p>
      ) : (
        <ul>
          {purchases.map(purchase => (
            <li key={purchase.purchaseId}>
                <p>Ürün: {purchase.product.productName}</p>
                <p>Satın Alma Tarihi: {formatDate(purchase.purchaseCreatedAt)}</p>
                <p>Ürün Ulaştı: {purchase.delivered ? 'Evet' : 'Hayır'}</p>
                <p>Yorum {purchase.isCommented ? 'Yapılmadı' : 'Yapıldı'}</p>
                {purchase.delivered && (
                    <p>Delivered At: {formatDate(purchase.purchaseDeliveredAt)}</p>
                )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default Purchases;