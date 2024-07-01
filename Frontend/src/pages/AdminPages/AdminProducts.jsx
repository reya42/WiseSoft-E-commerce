import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

const AdminProducts = () => {
    const [products, setProducts] = useState(null);
    const [loading, setLoading] = useState(false);
    const [page,setPage] = useState(0);

    const navigate = useNavigate();

    useEffect(() => {
        if (localStorage.getItem("user") == null || !JSON.parse(localStorage.getItem("user")).admin) {
            navigate("/");
        }
        const fetchProducts = async () => {
            setLoading(true);
            try {
                const response = await fetch(`http://localhost:8080/products?page=${page}&size=15&descending=productId`);
                if (!response.ok) {
                throw new Error('Network response was not ok');
                }
                const data = await response.json();
                setProducts(data);
            } 
            catch (error) {
                console.error('Error fetching search results:', error);
            } 
            finally {
                setLoading(false);
            }
        };

        fetchProducts();
    }, [page, localStorage.getItem("user")]);
    
    return (
        localStorage.getItem("user") != null && JSON.parse(localStorage.getItem("user")).admin && products && !loading ?
        <div className='adminpages_container'>
            <div className="adminpages_flex_container upper">
                <div className="admin_id">
                    ID
                </div>
                <div className="admin_brand">
                    Marka
                </div>
                <div className="admin_category">
                    Kategori Adı
                </div>
                <div className="admin_name">
                    Ürün Adı
                </div>
                <div className="admin_link">
                    Ürün Linki
                </div>
                <div className="admin_displayhome">
                    Ana Ekranda Gösterme
                </div>
                <div className="admin_hero">
                    Çok Satanlarda Gösterme
                </div>
                <div className="admin_shipping">
                    Gönderim Ücreti
                </div>
                <div className="admin_purchase">
                    Alış Fiyatı
                </div>
                <div className="admin_sell">
                    Satış Fiyatı
                </div>
                <div className="admin_discount">
                    İndirimli Satış Fiyatı
                </div>
                <div className="admin_stock">
                    Stok Adedi
                </div>
                <div className="admin_isnew">
                    Yeni Ürün
                </div>
                <div className="admin_isactive">
                    Aktif Ürün
                </div>
                <div className="admin_edit">
                    Düzenle
                </div>
            </div>
            {
                products.content.map(
                    (item) => {
                        return (
                            <div className="adminpages_flex_container">
                                <div className="admin_id">
                                    {item.productId}
                                </div>
                                <div className={`admin_brand ${item.brand == null && "color-red"}`}>
                                    {item.brand ? item.brand.brandName : "Yok"}
                                </div>
                                <div className={`admin_category ${item.category == null && "color-red"}`}>
                                    {item.category ? item.category.categoryName : "Yok" }
                                </div>
                                <div className="admin_name">
                                    {item.productName}
                                </div>
                                <div className="admin_link">
                                    {item.productLink}
                                </div>
                                <div className={`admin_displayhome${item.displayProductOnHomePage?" color-green":" color-red"}`}>
                                    {item.displayProductOnHomePage ? "Evet":"Hayır"}
                                </div>
                                <div className={`admin_hero${item.heroProduct?" color-green":" color-red"}`}>
                                    {item.heroProduct ? "Evet":"Hayır"}
                                </div>
                                <div className="admin_shipping">
                                    {item.thereShippingFee? `${item.productShippingFee}`: "0"} TL
                                </div>
                                <div className="admin_purchase">
                                    {item.productPurchasePrice} TL
                                </div>
                                <div className="admin_sell">
                                    {item.productSellPrice} TL
                                </div>
                                <div className="admin_discount">
                                    {item.productDiscountedPrice != -1 ? item.productDiscountedPrice: "0 "} TL
                                </div>
                                <div className="admin_stock">
                                    {item.productStock}
                                </div>
                                <div className={`admin_isnew${item.new?" color-green":" color-red"}`}>
                                    {item.new?"Evet":"Hayır"}
                                </div>
                                <div className={`admin_isactive${item.productActive?" color-green":" color-red"}`}>
                                    {item.productActive?"Evet":"Hayır"}
                                </div>
                                <div className="admin_edit">
                                    <Link to={`/admin/products/edit/${item.productLink}`}>
                                        Düzenle
                                    </Link>
                                </div>
                            </div>
                        )
                    }
                )
            }
            <div className='adminpages_page_controller'>
                <button className={products.first? "":"active"} 
                    onClick={() =>
                        {
                            if (!products.first) {
                                setPage(page-1);
                            }
                        }
                    }
                >
                    {"<"}
                </button>
                <div className={`adminpages_current_page${!products.last || !products.first ? " active":""}`}>
                    {page}
                </div>
                <button className={products.last? "":"active"} 
                    onClick={() =>
                        {
                            if (!products.last) {
                                setPage(page+1);
                            }
                        }
                    }
                >
                    {">"}
                </button>
            </div>
            <Link className='adminpages_addproduct' to={"/admin/products/add"}>
                Ürün Ekle +
            </Link>
        </div>
        :
        localStorage.getItem("user") != null && JSON.parse(localStorage.getItem("user")).admin  ?
        <div className="">
            Loading
        </div>
        :
        navigate("/")
    )
}

export default AdminProducts