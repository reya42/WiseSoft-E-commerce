import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Comment from '../components/Comment';
import './css/comments.css';

const Comments = () => {
    const { productLink } = useParams();

    const [comments, setComments] = useState([]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [product,setProduct] = useState(null);

    useEffect(() => {
        setLoading(true);
        const fetchProduct = async () => {
            try {
                const response = await fetch(`http://localhost:8080/products/${productLink}`);
                const data = await response.json();

                if (!response.ok) {
                    if (data.error != null) {
                        setError(data.error);
                        throw new Error(data.error);
                    }
                    else if (data.message != null) {
                        setError(data.message);
                        throw new Error(data.message);
                    }
                }
                else
                {
                    setProduct(data);
                    setLoading(false);
                    return data;
                }
            } catch (error) {
                console.error(error.message);
                return null;
            }
        };
        if (fetchProduct() != null)
        {
            const fetchComments = async () => {
                try {
                    setLoading(true);
                    const response = await fetch(`http://localhost:8080/products/${productLink}/comments?page=${page}&size=10`);
                    const data = await response.json();

                    if (!response.ok) {
                        setError(data.error || 'An error occurred while fetching comments.');
                        throw new Error(data.error || 'An error occurred while fetching comments.');
                    } else {
                        setComments(data);
                        setLoading(false);
                    }
                } catch (error) {
                    console.error(error.message);
                }
            };

            fetchComments();
        }
    }, [productLink, page]);

    return (
        <div className="comments_page_container">
            <div className="comments_product_container">
                <div className="comments_product_photo">
                    {
                        loading || !product ?
                            <div className='comment_image_container'> Yükleniyor </div>
                        :
                            product.images.length != 0 ?
                                <img src={`http://localhost:8080/images/${product.images[0].imageId}`} alt="" className='comment_image' />
                            :
                                <div className='comment_image_container'> Bu ürüne ait bir görsel bulunamadı </div>
                    }
                </div>
                <div className="comments_product_name">
                    {
                        loading || !product ? "Yükleniyor" : product.productName
                    }
                </div>
            </div>
            <div className="comments_comments_container">
                <div className="comments_title">
                    Ürüne Gelen Yorumlar
                </div>
                {
                    loading || !comments || !product ? "Yükleniyor" :
                    <div className='comments_comments_page_container'>
                        {
                            comments.content.map((item, index) => 
                            (
                                <Comment data={item} singleProduct={false}/>
                            )
                            )
                        }
                        <div className='comments_page'>
                            <button className={comments.first? "":"active"} 
                                onClick={() =>
                                    {
                                        if (!comments.first) {
                                            setPage(page-1);
                                        }
                                    }
                                }
                            >
                                {"<"}
                            </button>
                            <div className={`comment_current_page${!comments.last || !comments.first ? " active":""}`}>
                                {page}
                            </div>
                            <button className={comments.last? "":"active"} 
                                onClick={() =>
                                    {
                                        if (!comments.last) {
                                            setPage(page+1);
                                        }
                                    }
                                }
                            >
                                {">"}
                            </button>
                        </div>
                    </div>
                }
            </div>
        </div>
    );
};

export default Comments;