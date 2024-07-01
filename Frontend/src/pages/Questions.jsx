import React, {useState, useEffect} from 'react'
import { useParams } from 'react-router-dom';

import Question from '../components/Question';

import "./css/questions.css"
import CreateQuestionForm from '../components/CreateQuestionForm';


const Questions = () => {

    const {productLink} = useParams();

    const [questions, setQuestions] = useState();
    const [product,setProduct] = useState(null);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);

    useEffect(()=>{
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
            const fetchQuestions = async () => {
                try {
                    setLoading(true);
                    const response = await fetch(`http://localhost:8080/questions/${productLink}?size=10&page=${page}`);
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
                        setError("");
                        setQuestions(data);
                        setLoading(false);
                        return data;
                    }
                } catch (error) {
                    console.error(error.message);
                }
            };
            fetchQuestions();
        }
    },[])
    useEffect(()=>{
        const fetchQuestions = async () => {
            try {
                setLoading(true);
                const response = await fetch(`http://localhost:8080/questions/${productLink}?size=10&page=${page}`);
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
                    setError("");
                    setQuestions(data);
                    setLoading(false);
                    return data;
                }
            } catch (error) {
                console.error(error.message);
            }
        };
        fetchQuestions();
    },[page])

    if (error != "") {
        <div className="questions_page_container">
            {error}
        </div>
    }
    
    return (
        <div className="questions_page_container">
            <div className="questions_product_container">
                <div className="questions_product_photo">
                    {
                        loading || !product ?
                            <div className='question_image_container'> Yükleniyor </div>
                        :
                            product.images.length != 0 ?
                                <img src={`http://localhost:8080/images/${product.images[0].imageId}`} alt="" className='question_image' />
                            :
                                <div className='question_image_container'> Bu ürüne ait bir görsel bulunamadı </div>
                    }
                </div>
                <div className="questions_product_name">
                    {
                        loading || !product ? "Yükleniyor" : product.productName
                    }
                </div>
            </div>
            <div className="questions_questions_container">
                <div className="questions_title">
                    Ürünle İlgili Sorular
                </div>
                {
                    loading || !questions || !product ? "Yükleniyor" :
                    <div className='questions_questions_page_container'>
                        <CreateQuestionForm productId={product.productId}/>
                        {
                            questions.content.map((item, index) => 
                            (
                                <Question data={item} singleProduct={false}/>
                            )
                            )
                        }
                        <div className='questions_page'>
                            <button className={questions.first? "":"active"} 
                                onClick={() =>
                                    {
                                        if (!questions.first) {
                                            setPage(page-1);
                                        }
                                    }
                                }
                            >
                                {"<"}
                            </button>
                            <div className={`question_current_page${!questions.last || !questions.first ? " active":""}`}>
                                {page}
                            </div>
                            <button className={questions.last? "":"active"} 
                                onClick={() =>
                                    {
                                        if (!questions.last) {
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
    )
}

export default Questions